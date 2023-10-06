package oracleai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

@RestController
@RequestMapping("/askai")

public class AskAIController {

    private static Logger log = LoggerFactory.getLogger(AskAIController.class);


    public static class QuestionForm {
        private String question;
        public String getQuestion() {
            return question;
        }
        public void setQuestion(String question) {
            this.question = question;
        }
    }


    @CrossOrigin
    @PostMapping("/askai")
    public ResponseEntity<String> receiveQuestion(@RequestBody QuestionForm questionForm) throws Exception {
        System.out.println("ExplainAndAdviseOnHealthTestResults.receiveQuestion:" +
                questionForm.getQuestion());
        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Accept", "application/json");
        headers.set("Authorization", "Bearer " + System.getenv("COHERE_KEY"));
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
        ResponseEntity<String> stringResponseEntity = new ResponseEntity<>(text, HttpStatus.OK);
        return stringResponseEntity;
    }



    //for testing without frontend only...



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


                "test" +


                "</body>" +
                "</html>";
    }


}



