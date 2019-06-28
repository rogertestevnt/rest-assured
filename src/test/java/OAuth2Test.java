import io.restassured.response.Response;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static io.restassured.RestAssured.given;

public class OAuth2Test {

    private final static String tokenUrl = "";
    private final static String businessUrl = "";
    private final static String username= "";
    private final static String password = "";
    private final static String clientId = "";
    private final static String clientSecret = "";
    private final static String scope = "";

    private final Logger logger = LoggerFactory.getLogger(OAuth2Test.class);

    private String resourceOwnerLogin(String tokenUri, String clientId, String clientSecret, String username, String password, String scope) throws JSONException {
        logger.info("Getting OAuth Token from server - {}", tokenUri);
        Response response =
                given().auth().preemptive().basic(clientId, clientSecret)
                        .formParam("grant_type", "password")
                        .formParam("username", username)
                        .formParam("password", password)
                        .formParam("scope", scope)
                        .when()
                        .post(tokenUri);

        JSONObject jsonObject = new JSONObject(response.getBody().asString());
        String accessToken = jsonObject.get("access_token").toString();
        String tokenType = jsonObject.get("token_type").toString();
        logger.info("Oauth Token for {} with type {} is {}", username, tokenType, accessToken);
        return accessToken;
    }

    public void callAddDuplicatedPoints(String accessToken) {
        //language=JSON
        String jsonString = "{\"productId\":8,\"code\":\"200002020207\"}";
        Response response = given().auth().preemptive().oauth2(accessToken)
                .contentType("application/json")
                .body(jsonString)
                .when()
                .post(businessUrl + "api/private/manager/customer/addPoint");
        String responseBody = response.getBody().asString();
        assert(responseBody.contains("\"exception\":\"PointDuplicatedException\""));
        if (response.getStatusCode() == 409) {
            logger.info("Success = " + responseBody);
        } else {
            logger.error("Fail = {}", responseBody);
        }
    }

    private String clientCredentialsLogin(String tokenUri, String clientId, String clientSecret, String scope) throws JSONException {
        logger.info("Getting OAuth Token from server - {}", tokenUri);
        Response response =
                given().auth().preemptive().basic(clientId, clientSecret)
                        .formParam("grant_type", "client_credentials")
                        .formParam("scope", scope)
                        .when()
                        .post(tokenUri);

        JSONObject jsonObject = new JSONObject(response.getBody().asString());
        String accessToken = jsonObject.get("access_token").toString();
        String tokenType = jsonObject.get("token_type").toString();
        logger.info("Oauth Token with type {} is {}", tokenType, accessToken);
        return accessToken;
    }

    public void getDataFromResourceServer(String accessToken) {
        Response response = given().auth().preemptive().oauth2(accessToken)
                .contentType("application/json")
                .when()
                .get(businessUrl + "api/private/manager/customer/balance");
        String responseBody = response.getBody().asString();
        if (responseBody.contains("accumulatedPoints")){
            System.out.println("Success");
        }

        if (response.getStatusCode() == 200) {
            logger.info("Successfull Response = " + responseBody);
        } else {
            logger.error("UnSuccessfull Response = = {}", responseBody);
        }
    }

    @Test
    public void testAuthWithResourceOwnerCredentials() throws JSONException {
        final String accessToken = resourceOwnerLogin(tokenUrl, clientId, clientSecret, username, password, scope);
        logger.info("AccessToken = {}", accessToken);
        //TODO: Whatever you want to do with access token now
        getDataFromResourceServer(accessToken);
        callAddDuplicatedPoints(accessToken);
    }

    @Ignore
    @Test
    public void testAuthWithClientCredentials() throws JSONException {
        final String accessToken = clientCredentialsLogin(tokenUrl, clientId, clientSecret, scope);
        logger.info("AccessToken = {}", accessToken);
        //TODO: Whatever you want to do with access token now
        getDataFromResourceServer(accessToken);

    }

}

