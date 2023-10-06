package oracleai;

import com.theokanning.openai.image.CreateImageRequest;
import com.theokanning.openai.service.OpenAiService;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import javax.sound.sampled.*;
import java.io.*;
import java.time.Duration;
import java.util.*;

@RestController
@RequestMapping("/medicaltranscript")
public class MedicalTranscriptionsController {

    static List<String> storyImages = new ArrayList();

    @GetMapping("/form")
    public String form(){
        return "<!DOCTYPE html>" +
                "<html>" +
                "<head>" +
                "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">" +
                "<style>" +
                "   body {" +
                "      font-family: Arial, sans-serif;" +
                "    }" +
                ".button {" +
                "  background-color: #ddd;" +
                "  border: none;" +
                "  color: black;" +
                "  padding: 10px 20px;" +
                "  text-align: center;" +
                "  text-decoration: none;" +
                "  display: inline-block;" +
                "  margin: 4px 2px;" +
                "  cursor: pointer;" +
                "  border-radius: 16px;" +
                "}" +
                "" +
                ".button:hover {" +
                "  background-color: #f1f1f1;" +
                "}" +
                "</style>" +
                "</head>" +
                "<body>" +



                "<form method=\"post\" action=\"/medicaltranscript\" enctype=\"multipart/form-data\">" +
//                " <br> <br>   Model Name:<br>" +
//                "<select name=\"model\" id=\"model\">" +
//                "  <option value=\"breastcancer\">Breast Cancer and Normal Breast Model</option>" +
//                "  <option value=\"covid\">Covid, Pneumonia, and Normal Chest Model</option>" +
//                "  <option value=\"mercedes\">Lung Cancer and Normal Lung Model</option>" +
//                "</select>" +
                " <br> <br> <br>   Audio File:" +
                " <br>    <input type=\"file\" name=\"file\" accept=\"audio/*\">" +
                "    <br> <br>  <br>  " +
                "<button type=\"submit\" class=\"button\">Process Audio</button>" +
                "</form>" +
                "<br>  " +
                "<a href=\"XRay-Sample-Data.zip\">Download Sample Data</a>" +


                "</body>" +
                "</html>" ;
    }


    @GetMapping("/medicaltranscript")
    public String medicaltranscript(@RequestParam("file") MultipartFile file) throws Exception {
        AudioFormat format =
                new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, 44100.0f, 16, 1,
                        (16 / 8) * 1, 44100.0f, true);
        SoundRecorder soundRecorder = new SoundRecorder();
        soundRecorder.build(format);
        System.out.println("Start recording ....");
        soundRecorder.start();
        Thread.sleep(8000);
        soundRecorder.stop();
        System.out.println("Stopped recording ....");
        Thread.sleep(3000); //give the process time
        String name = "AISoundClip";
        AudioFileFormat.Type fileType = AudioFileFormat.Type.WAVE;
        AudioInputStream audioInputStream = soundRecorder.audioInputStream;
        System.out.println("Saving...");
        audioInputStream.reset();
//        AudioSystem.write(audioInputStream, fileType, file);
//        System.out.println("Saved file " + file.getAbsolutePath());
        String transcription = transcribe(file);
        System.out.println("transcription " + transcription);
        String imageLocation = imagegeneration(transcription);
        System.out.println("imageLocation " + imageLocation);
        storyImages.add(imageLocation);
        String htmlStoryFrames = "";
        Iterator<String> iterator = storyImages.iterator();
        while(iterator.hasNext()) {
            htmlStoryFrames += "<td><img src=\"" + iterator.next() +"\" width=\"400\" height=\"400\"></td>";
        }
        return htmlStoryFrames;

    }

    public String imagegeneration(String imagedescription) throws Exception {
        OpenAiService service =
                new OpenAiService(System.getenv("OPENAI_KEY"), Duration.ofSeconds(60));
        CreateImageRequest openairequest = CreateImageRequest.builder()
                .prompt(imagedescription)
                .build();

        System.out.println("\nImage is located at:");
        String imageLocation = service.createImage(openairequest).getData().get(0).getUrl();
        service.shutdownExecutor();
     return imageLocation;
    }


    public String transcribe(MultipartFile file) throws Exception {
        OpenAiService service =
                new OpenAiService(System.getenv("OPENAI_KEY"), Duration.ofSeconds(60));
        String audioTranscription = transcribeFile(file, service);
        service.shutdownExecutor();
        return audioTranscription;
    }
    private String transcribeFile(MultipartFile file, OpenAiService service) throws Exception
    {
        String endpoint = "https://api.openai.com/v1/audio/transcriptions";
        String modelName = "whisper-1";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        headers.setBearerAuth(System.getenv("OPENAI_KEY"));
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        byte[] fileBytes = new byte[0];
        try (InputStream fis = file.getInputStream();
             ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = fis.read(buffer)) != -1) {
                bos.write(buffer, 0, bytesRead);
            }
            fileBytes = bos.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
        }
        body.add("file", new ByteArrayResource(fileBytes) {
            @Override
            public String getFilename() {
                return file.getName();
            }
        });
        body.add("model", modelName);
        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> response = restTemplate.exchange(endpoint, HttpMethod.POST, requestEntity, String.class);
        return response.getBody();
    }

    public class SoundRecorder implements Runnable {
        AudioInputStream audioInputStream;
        private AudioFormat format;
        public Thread thread;


        public SoundRecorder build(AudioFormat format) {
            this.format = format;
            return this;
        }

        public void start() {
            thread = new Thread(this);
            thread.start();
        }

        public void stop() {
            thread = null;
        }

        @Override
        public void run() {
            try (final ByteArrayOutputStream out = new ByteArrayOutputStream(); final TargetDataLine line = getTargetDataLineForRecord();) {
                int frameSizeInBytes = format.getFrameSize();
                int bufferLengthInFrames = line.getBufferSize() / 8;
                final int bufferLengthInBytes = bufferLengthInFrames * frameSizeInBytes;
                buildByteOutputStream(out, line, frameSizeInBytes, bufferLengthInBytes);
                this.audioInputStream = new AudioInputStream(line);
                setAudioInputStream(convertToAudioIStream(out, frameSizeInBytes));
                audioInputStream.reset();
            } catch (IOException ex) {
                ex.printStackTrace();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        public void buildByteOutputStream(final ByteArrayOutputStream out, final TargetDataLine line, int frameSizeInBytes, final int bufferLengthInBytes) throws IOException {
            final byte[] data = new byte[bufferLengthInBytes];
            int numBytesRead;

            line.start();
            while (thread != null) {
                if ((numBytesRead = line.read(data, 0, bufferLengthInBytes)) == -1) {
                    break;
                }
                out.write(data, 0, numBytesRead);
            }
        }

        private void setAudioInputStream(AudioInputStream aStream) {
            this.audioInputStream = aStream;
        }

        public AudioInputStream convertToAudioIStream(final ByteArrayOutputStream out, int frameSizeInBytes) {
            byte[] audioBytes = out.toByteArray();
            AudioInputStream audioStream =
                    new AudioInputStream(new ByteArrayInputStream(audioBytes), format,
                            audioBytes.length / frameSizeInBytes);
            System.out.println("Recording finished");
            return audioStream;
        }

        public TargetDataLine getTargetDataLineForRecord() {
            TargetDataLine line;
            DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);
            if (!AudioSystem.isLineSupported(info)) {
                return null;
            }
            try {
                line = (TargetDataLine) AudioSystem.getLine(info);
                line.open(format, line.getBufferSize());
            } catch (final Exception ex) {
                return null;
            }
            return line;
        }
    }

}
