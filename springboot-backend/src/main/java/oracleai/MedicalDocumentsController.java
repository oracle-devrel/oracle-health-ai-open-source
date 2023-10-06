package oracleai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import com.oracle.bmc.Region;
import com.oracle.bmc.aivision.AIServiceVisionClient;
import com.oracle.bmc.aivision.model.*;
import com.oracle.bmc.aivision.requests.AnalyzeImageRequest;
import com.oracle.bmc.aivision.responses.AnalyzeImageResponse;
import com.oracle.bmc.auth.AuthenticationDetailsProvider;
import com.oracle.bmc.auth.ConfigFileAuthenticationDetailsProvider;
import com.oracle.bmc.auth.InstancePrincipalsAuthenticationDetailsProvider;
import com.theokanning.openai.completion.chat.ChatCompletionChoice;
import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.completion.chat.ChatMessageRole;
import com.theokanning.openai.service.OpenAiService;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

@RestController
@RequestMapping("/medicaldocuments")
public class MedicalDocumentsController {

    private static Logger log = LoggerFactory.getLogger(MedicalDocumentsController.class);

    @GetMapping("/form")
    public String form() {
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


                "<form method=\"post\" action=\"/health/analyzedoc\" enctype=\"multipart/form-data\">" +
                " <br> <br> <br> <br> <br>   Select A Receipt:" +
                " <br>    <input type=\"file\" name=\"file\" accept=\"image/*\">" +
                "    <br>" +
                "    <br><input type=\"submit\" value=\"Process Receipt\">" +
                "</form>" +
                "<br>  " +
                "<a href=\"XRay-Sample-Data.zip\">Download Sample Data</a>" +


                "</body>" +
                "</html>";
    }



    public class AskAIDTO {
        private String question;
        private String imageLocation;
        private String selectedItem;

        // Getters and setters
        public String getQuestion() {
            return question;
        }

        public void setQuestion(String question) {
            this.question = question;
        }

        public String getImageLocation() {
            return imageLocation;
        }

        public void setImageLocation(String imageLocation) {
            this.imageLocation = imageLocation;
        }

        public String getSelectedItem() {
            return selectedItem;
        }

        public void setSelectedItem(String selectedItem) {
            this.selectedItem = selectedItem;
        }
    }

    @PostMapping("/analyzedocformodel")
    public ResponseEntity<String> askAI(
            @RequestParam("model") String model,
            @RequestParam("imageFile") MultipartFile imageFile) throws Exception {

        // Process the incoming data
        System.out.println("model: " + model);
        System.out.println("Received file: " + imageFile.getOriginalFilename());
        String objectDetectionResults = processImage(imageFile.getBytes(), true);
        System.out.println("ExplainAndAdviseOnHealthTestResults.askAI objectDetectionResults:" + objectDetectionResults);
        return ResponseEntity.ok(objectDetectionResults);
    }

    @PostMapping("/analyzedoc")
    public String analyzeimage(@RequestParam("model") String model, @RequestParam("file") MultipartFile file)
            throws Exception {
        log.info("analyzing image file:" + file);
        String objectDetectionResults = processImage(file.getBytes(), true);
        return "<html><br><br>objectDetectionResults:" + objectDetectionResults + "</html>";
    }

    @PostMapping("/analyzedoc0")
    public String analyzedoc0(@RequestParam("file") MultipartFile file)
            throws Exception {
        log.info("analyzing image file:" + file);
        String objectDetectionResults = processImage(file.getBytes(), true);
        ImageAnalysis imageAnalysis = parseJsonToImageAnalysis(objectDetectionResults);
        List<Line> lines = imageAnalysis.getImageText().getLines();
        String fullText = "";
        for (Line line : lines) fullText += line.getText();
        log.info("fullText = " + fullText);
        String explanationOfResults = chat("explain these test results in simple terms " +
                "and tell me what should I do to get better results: \"" + fullText + "\"");
//        return new ResponseEntity<>(text, HttpStatus.OK);
        return "<html><br><br>explanationOfResults:" + explanationOfResults + "</html>";
    }


    public static class QuestionForm {
        private String question;

        public String getQuestion() {
            return question;
        }

        public void setQuestion(String question) {
            this.question = question;
        }
    }

    @PostMapping("/askai")
    public ResponseEntity<String> receiveQuestion(@RequestBody QuestionForm questionForm) throws Exception {
        System.out.println("ExplainAndAdviseOnHealthTestResults.receiveQuestion:" +
                questionForm.getQuestion());
        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Accept", "application/json");
        headers.set("Authorization", "Bearer oJfPTyourtoken9Ji1VRz7");

        String body = "{\"max_tokens\":200,\"truncate\":\"END\",\"return_likelihoods\":\"NONE\"," +
                "\"prompt\":\"" + questionForm.getQuestion() + "\"}";

        HttpEntity<String> entity = new HttpEntity<>(body, headers);

        ResponseEntity<String> response = restTemplate.exchange(
                "https://api.cohere.ai/v1/generate",
                HttpMethod.POST,
                entity,
                String.class);

        System.out.println(response.getBody());
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode root = objectMapper.readTree(response.getBody());
        String text = root.path("generations").get(0).path("text").asText();
        System.out.println("Text: " + text);
        return new ResponseEntity<>(text, HttpStatus.OK);

    }


    String chat(String textcontent) {
        OpenAiService service =
                new OpenAiService(System.getenv("OPENAI_KEY"), Duration.ofSeconds(60));
        System.out.println("Streaming chat completion... textcontent:" + textcontent);
        final List<ChatMessage> messages = new ArrayList<>();
        final ChatMessage systemMessage = new ChatMessage(ChatMessageRole.SYSTEM.value(), textcontent);
        messages.add(systemMessage);
        ChatCompletionRequest chatCompletionRequest = ChatCompletionRequest
                .builder()
                .model("gpt-3.5-turbo")
                .messages(messages)
                .n(1)
                .maxTokens(300) //was 50
                .logitBias(new HashMap<>())
                .build();
        String replyString = "";
        String content;
        for (ChatCompletionChoice choice : service.createChatCompletion(chatCompletionRequest).getChoices()) {
            content = choice.getMessage().getContent();
            replyString += (content == null ? " " : content);
        }
        service.shutdownExecutor();
        return replyString;
    }

    String processImage(byte[] bytes, boolean isConfigFileAuth) throws Exception {
        AIServiceVisionClient aiServiceVisionClient;
        AuthenticationDetailsProvider provider;
        if (isConfigFileAuth) {
//            provider = AuthDetailsProviderFactory.getAuthenticationDetailsProvider();
          provider =          new ConfigFileAuthenticationDetailsProvider(
                     System.getenv("OCICONFIG_FILE"),System.getenv("OCICONFIG_PROFILE"));
            System.out.println("ExplainAndAdviseOnHealthTestResults.processImage provider:" + provider);
            aiServiceVisionClient = AIServiceVisionClient.builder().build(provider);
            aiServiceVisionClient.setRegion(Region.US_PHOENIX_1);
            System.out.println("ExplainAndAdviseOnHealthTestResults.processImage aiServiceVisionClient:" + aiServiceVisionClient);

        } else {
            aiServiceVisionClient = AIServiceVisionClient.builder().build(
                    InstancePrincipalsAuthenticationDetailsProvider.builder().build());
        }
        List<ImageFeature> features = new ArrayList<>();

//        custom_image_classification_model_ocid = "ocid1.aivisionmodel.oc1.iad.amaaaaaaq33dybyarzggwrusdn5i6djim476cplqldqms4pa3int2rs476na"
//# setup request body with image classification
//        image_classification_feature = oci.ai_vision.models.ImageClassificationFeature()
//        image_classification_feature.model_id = custom_image_classification_model_ocid
//        image_classification_feature.max_results = MAX_RESULTS

        ImageFeature classifyFeature = ImageClassificationFeature.builder()
                .modelId("ocid1.aivisionmodel.oc1.phx.amaaaaaaknuwtjiacgxrmoatfmiubv4i25cmrmosccwccmaeo3yk4fzvxrwa")
                .maxResults(10)
                .build();
//        ImageFeature detectImageFeature = ImageObjectDetectionFeature.builder()
//                .maxResults(10)
//                .build();
//        ImageFeature textDetectImageFeature = ImageTextDetectionFeature.builder().build();
        features.add(classifyFeature);
//        features.add(detectImageFeature);
//        features.add(textDetectImageFeature);
        InlineImageDetails inlineImageDetails = InlineImageDetails.builder()
                .data(bytes)
                .build();
        AnalyzeImageDetails analyzeImageDetails = AnalyzeImageDetails.builder()
                .image(inlineImageDetails)
                .features(features)
                .build();
        AnalyzeImageRequest request = AnalyzeImageRequest.builder()
                .analyzeImageDetails(analyzeImageDetails)
                .build();
        AnalyzeImageResponse response = aiServiceVisionClient.analyzeImage(request);
        ObjectMapper mapper = new ObjectMapper();
        mapper.setFilterProvider(new SimpleFilterProvider().setFailOnUnknownId(false));

        String json = mapper.writeValueAsString(response.getAnalyzeImageResult());
        System.out.println("AnalyzeImage Result");
        System.out.println(json);
        return json;
    }

    String processImage0(byte[] bytes, boolean isConfigFileAuth) throws Exception {
        AIServiceVisionClient aiServiceVisionClient;
        AuthenticationDetailsProvider provider;
        if (isConfigFileAuth) {
            provider = new ConfigFileAuthenticationDetailsProvider(
                    System.getenv("OCICONFIG_FILE"), System.getenv("OCICONFIG_PROFILE"));
            aiServiceVisionClient = AIServiceVisionClient.builder().build(provider);
        } else {
            aiServiceVisionClient = new AIServiceVisionClient(InstancePrincipalsAuthenticationDetailsProvider.builder().build());
        }
        List<ImageFeature> features = new ArrayList<>();
        ImageFeature classifyFeature = ImageClassificationFeature.builder()
                .maxResults(10)
                .build();
        ImageFeature detectImageFeature = ImageObjectDetectionFeature.builder()
                .maxResults(10)
                .build();
        ImageFeature textDetectImageFeature = ImageTextDetectionFeature.builder().build();
        features.add(classifyFeature);
        features.add(detectImageFeature);
        features.add(textDetectImageFeature);
        InlineImageDetails inlineImageDetails = InlineImageDetails.builder()
                .data(bytes)
                .build();
        AnalyzeImageDetails analyzeImageDetails = AnalyzeImageDetails.builder()
                .image(inlineImageDetails)
                .features(features)
                .build();
        AnalyzeImageRequest request = AnalyzeImageRequest.builder()
                .analyzeImageDetails(analyzeImageDetails)
                .build();
        AnalyzeImageResponse response = aiServiceVisionClient.analyzeImage(request);
        ObjectMapper mapper = new ObjectMapper();
        mapper.setFilterProvider(new SimpleFilterProvider().setFailOnUnknownId(false));

        String json = mapper.writeValueAsString(response.getAnalyzeImageResult());
        System.out.println("AnalyzeImage Result");
        System.out.println(json);
        return json;
    }

    @Data
    class ImageObject {
        private String name;
        private double confidence;
        private BoundingPolygon boundingPolygon;
    }

    @Data
    class BoundingPolygon {
        private List<Point> normalizedVertices;
    }

    @Data
    class Point {
        private double x;
        private double y;

        public Point(double x, double y) {
            this.x = x;
            this.y = y;
        }
    }

    @Data
    class Label {
        private String name;
        private double confidence;
    }

    @Data
    class OntologyClass {
        private String name;
        private List<String> parentNames;
        private List<String> synonymNames;
    }

    @Data
    class ImageText {
        private List<Word> words;
        private List<Line> lines;
    }

    @Data
    class Word {
        private String text;
        private double confidence;
        private BoundingPolygon boundingPolygon;
    }

    @Data
    class Line {
        private String text;
        private double confidence;
        private BoundingPolygon boundingPolygon;
        private List<Integer> wordIndexes;
    }

    @Data
    class ImageAnalysis {
        private List<ImageObject> imageObjects;
        private List<Label> labels;
        private List<OntologyClass> ontologyClasses;
        private ImageText imageText;
        private String imageClassificationModelVersion;
        private String objectDetectionModelVersion;
        private String textDetectionModelVersion;
        private List<String> errors;
    }


    private ImageAnalysis parseJsonToImageAnalysis(String jsonString) {
        JSONObject json = new JSONObject(jsonString);
        JSONArray imageObjectsArray = json.getJSONArray("imageObjects");
        List<ImageObject> imageObjects = new ArrayList<>();
        for (int i = 0; i < imageObjectsArray.length(); i++) {
            JSONObject imageObjectJson = imageObjectsArray.getJSONObject(i);
            oracleai.MedicalDocumentsController.ImageObject imageObject = new ImageObject();
            imageObject.setName(imageObjectJson.getString("name"));
            imageObject.setConfidence(imageObjectJson.getDouble("confidence"));

            JSONObject boundingPolygonJson = imageObjectJson.getJSONObject("boundingPolygon");
            JSONArray normalizedVerticesArray = boundingPolygonJson.getJSONArray("normalizedVertices");
            List<Point> normalizedVertices = new ArrayList<>();
            for (int j = 0; j < normalizedVerticesArray.length(); j++) {
                JSONObject vertexJson = normalizedVerticesArray.getJSONObject(j);
                Point vertex = new Point(vertexJson.getDouble("x"), vertexJson.getDouble("y"));
                normalizedVertices.add(vertex);
            }
            BoundingPolygon boundingPolygon = new BoundingPolygon();
            boundingPolygon.setNormalizedVertices(normalizedVertices);
            imageObject.setBoundingPolygon(boundingPolygon);

            imageObjects.add(imageObject);
        }
        JSONArray labelsArray = json.getJSONArray("labels");
        List<Label> labels = new ArrayList<>();
        for (int i = 0; i < labelsArray.length(); i++) {
            JSONObject labelJson = labelsArray.getJSONObject(i);
            Label label = new Label();
            label.setName(labelJson.getString("name"));
            label.setConfidence(labelJson.getDouble("confidence"));
            labels.add(label);
        }
        JSONArray ontologyClassesArray = json.getJSONArray("ontologyClasses");
        List<OntologyClass> ontologyClasses = new ArrayList<>();
        for (int i = 0; i < ontologyClassesArray.length(); i++) {
            JSONObject ontologyClassJson = ontologyClassesArray.getJSONObject(i);
            OntologyClass ontologyClass = new OntologyClass();
            ontologyClass.setName(ontologyClassJson.getString("name"));
            JSONArray parentNamesArray = ontologyClassJson.getJSONArray("parentNames");
            List<String> parentNames = new ArrayList<>();
            for (int j = 0; j < parentNamesArray.length(); j++) {
                parentNames.add(parentNamesArray.getString(j));
            }
            ontologyClass.setParentNames(parentNames);
            ontologyClasses.add(ontologyClass);
        }
        JSONObject imageTextJson = json.getJSONObject("imageText");
        JSONArray wordsArray = imageTextJson.getJSONArray("words");
        List<Word> words = new ArrayList<>();
        for (int i = 0; i < wordsArray.length(); i++) {
            JSONObject wordJson = wordsArray.getJSONObject(i);
            Word word = new Word();
            word.setText(wordJson.getString("text"));
            word.setConfidence(wordJson.getDouble("confidence"));
            JSONObject boundingPolygonJson = wordJson.getJSONObject("boundingPolygon");
            JSONArray normalizedVerticesArray = boundingPolygonJson.getJSONArray("normalizedVertices");
            List<Point> normalizedVertices = new ArrayList<>();
            for (int j = 0; j < normalizedVerticesArray.length(); j++) {
                JSONObject vertexJson = normalizedVerticesArray.getJSONObject(j);
                Point vertex = new Point(vertexJson.getDouble("x"), vertexJson.getDouble("y"));
                normalizedVertices.add(vertex);
            }
            BoundingPolygon boundingPolygon = new BoundingPolygon();
            boundingPolygon.setNormalizedVertices(normalizedVertices);
            word.setBoundingPolygon(boundingPolygon);
            words.add(word);
        }
        JSONArray linesArray = imageTextJson.getJSONArray("lines");
        List<Line> lines = new ArrayList<>();
        for (int i = 0; i < linesArray.length(); i++) {
            JSONObject lineJson = linesArray.getJSONObject(i);
            Line line = new Line();
            line.setText(lineJson.getString("text"));
            line.setConfidence(lineJson.getDouble("confidence"));
            JSONObject boundingPolygonJson = lineJson.getJSONObject("boundingPolygon");
            JSONArray normalizedVerticesArray = boundingPolygonJson.getJSONArray("normalizedVertices");
            List<Point> normalizedVertices = new ArrayList<>();
            for (int j = 0; j < normalizedVerticesArray.length(); j++) {
                JSONObject vertexJson = normalizedVerticesArray.getJSONObject(j);
                Point vertex = new Point(vertexJson.getDouble("x"), vertexJson.getDouble("y"));
                normalizedVertices.add(vertex);
            }
            BoundingPolygon boundingPolygon = new BoundingPolygon();
            boundingPolygon.setNormalizedVertices(normalizedVertices);
            line.setBoundingPolygon(boundingPolygon);
            JSONArray wordIndexesArray = lineJson.getJSONArray("wordIndexes");
            List<Integer> wordIndexes = new ArrayList<>();
            for (int j = 0; j < wordIndexesArray.length(); j++) {
                wordIndexes.add(wordIndexesArray.getInt(j));
            }
            line.setWordIndexes(wordIndexes);
            lines.add(line);
        }

        String imageClassificationModelVersion = json.getString("imageClassificationModelVersion");
        String objectDetectionModelVersion = json.getString("objectDetectionModelVersion");
        String textDetectionModelVersion = json.getString("textDetectionModelVersion");
        List<String> errors = new ArrayList<>();
        JSONArray errorsArray = json.getJSONArray("errors");
        for (int i = 0; i < errorsArray.length(); i++) {
            errors.add(errorsArray.getString(i));
        }

        ImageText imageText = new ImageText();
        imageText.setWords(words);
        imageText.setLines(lines);

        ImageAnalysis imageAnalysis = new ImageAnalysis();
        imageAnalysis.setImageObjects(imageObjects);
        imageAnalysis.setLabels(labels);
        imageAnalysis.setOntologyClasses(ontologyClasses);
        imageAnalysis.setImageText(imageText);
        imageAnalysis.setImageClassificationModelVersion(imageClassificationModelVersion);
        imageAnalysis.setObjectDetectionModelVersion(objectDetectionModelVersion);
        imageAnalysis.setTextDetectionModelVersion(textDetectionModelVersion);
        imageAnalysis.setErrors(errors);

        return imageAnalysis;
    }
}




