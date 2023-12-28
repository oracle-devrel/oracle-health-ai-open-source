package oracleai;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.theokanning.openai.image.CreateImageRequest;
import com.theokanning.openai.service.OpenAiService;
import oracleai.services.ImageGeneration;
import oracleai.services.OracleGenAI;
import oracleai.services.OracleObjectStore;
import oracleai.services.OracleSpeechAI;
import org.jetbrains.annotations.NotNull;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.ui.Model;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import javax.sound.sampled.*;
import java.io.*;
import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/medicaltranscript")
public class MedicalTranscriptionsController {

    @PostMapping("/medicaltranscript")
    public String medicaltranscript(@RequestParam("file") MultipartFile multipartFile) throws Exception {
        OracleObjectStore.sendToObjectStorage(multipartFile.getOriginalFilename(), multipartFile.getInputStream());
        String transcriptionJobId = OracleSpeechAI.getTranscriptFromOCISpeech(multipartFile.getOriginalFilename());
        System.out.println("transcriptionJobId: " + transcriptionJobId);
        String jsonTranscriptFromObjectStorage =
                OracleObjectStore.getFromObjectStorage(transcriptionJobId,
                        AIApplication.OBJECTSTORAGE_NAMESPACE + "_" +
                                AIApplication.OBJECTSTORAGE_BUCKETNAME + "_" +
                                multipartFile.getOriginalFilename() + ".json");
//        System.out.println("jsonTranscriptFromObjectStorage: " + jsonTranscriptFromObjectStorage);
        String textFromTranscript  = getConcatenatedTokens(jsonTranscriptFromObjectStorage);
        System.out.println("textFromTranscript: " + textFromTranscript);
        String answerFromGenAI = OracleGenAI.chat(textFromTranscript + " in 40 words or less");
        return answerFromGenAI + " This advice is not meant as a substitute for professional healthcare guidance.";
    }


    public String getConcatenatedTokens(String json) throws JsonProcessingException{
            OracleSpeechAI.TranscriptionResponse response =
                    new ObjectMapper().readValue(json, OracleSpeechAI.TranscriptionResponse.class);
            return response.getTranscriptions().stream()
                    .flatMap(transcription -> transcription.getTokens().stream())
                    .map(OracleSpeechAI.TranscriptionResponse.Transcription.Token::getToken)
                    .collect(Collectors.joining(" "));
    }
}
