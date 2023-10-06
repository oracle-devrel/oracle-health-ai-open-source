package oracleai;

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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/imageanalysis")
public class ImageAnalysisController {

    private static Logger log = LoggerFactory.getLogger(ImageAnalysisController.class);

    @CrossOrigin
    @PostMapping("/analyzeimage")
    public String analyzeimage(@RequestParam("model") String model, @RequestParam("file") MultipartFile file)
            throws Exception {
        log.info("analyzing image file:" + file);
        return processImage(file.getBytes(), true);
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

    //For testing without front end from here to end...


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


                "<form method=\"post\" action=\"/imageanalysis/analyzeimage\" enctype=\"multipart/form-data\">" +
                " <br> <br>   Model Name:<br>" +
                "<select name=\"model\" id=\"model\">" +
                "  <option value=\"breastcancer\">Breast Cancer and Normal Breast Model</option>" +
                "  <option value=\"covid\">Covid, Pneumonia, and Normal Chest Model</option>" +
                "  <option value=\"mercedes\">Lung Cancer and Normal Lung Model</option>" +
                "</select>" +
                " <br> <br> <br>   Image File:" +
                " <br>    <input type=\"file\" name=\"file\" accept=\"image/*\">" +
                "    <br> <br>  <br>  " +
                "<button type=\"submit\" class=\"button\">Process Image</button>" +
                "</form>" +
                "<br>  " +
                "<a href=\"XRay-Sample-Data.zip\">Download Sample Data</a>" +


                "</body>" +
                "</html>";
    }


    @GetMapping("/formmedicaldoc")
    public String formmedicaldoc() {
        return "<html><form method=\"post\" action=\"/health/analyzedoc\" enctype=\"multipart/form-data\">" +
                " <br> <br> <br> <br> <br>   Select A Receipt:" +
                " <br>    <input type=\"file\" name=\"file\" accept=\"image/*\">" +
                "    <br>" +
                "    <br><input type=\"submit\" value=\"Process Receipt\">" +
                "</form>" +
                "<br> <br> " +
                "<a href=\"XRay-Sample-Data.zip\">Download Sample Data</a></html>" +
                "</form></html>";
    }
}



