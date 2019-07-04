import com.google.gson.JsonObject;
import io.restassured.response.Response;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import utils.ReadJsonFile;

import java.io.FileNotFoundException;

import static io.restassured.RestAssured.given;

public class OAuth2Test {

    private static String tokenUrl;
    private static String businessUrl;
    private static String username ;
    private static String password ;
    private static String clientId;
    private static String clientSecret;
    private static String scope;

    private final Logger logger = LoggerFactory.getLogger(OAuth2Test.class);


    @Before
    public void InitializeVariables() throws FileNotFoundException{

        JsonObject jsonObject = ReadJsonFile.readJson();

        businessUrl =  jsonObject.get("businessUrl").getAsString();
        username = jsonObject.get("email").getAsString();
        password = jsonObject.get("password").getAsString();
        clientId = jsonObject.get("clientId").getAsString();
        clientSecret = jsonObject.get("clientSecret").getAsString();
        scope = jsonObject.get("scope").getAsString();
        tokenUrl = jsonObject.get("tokenUrl").getAsString();

    }

    @Test
    public void testAuthWithResourceOwnerCredentials() throws JSONException {

        final String accessToken = resourceOwnerLogin(tokenUrl, clientId, clientSecret, username, password, scope);
        getCustomerPointsBalance(accessToken);
        callAddDuplicatedPoints(accessToken);
    }

    @Ignore
    @Test
    public void testAuthWithClientCredentials() throws JSONException {
        final String accessToken = clientCredentialsLogin(tokenUrl, clientId, clientSecret, scope);
        logger.info("AccessToken = {}", accessToken);
    }

    /**
     * This method performs the login based on user credentials and returns the access token
     * The variables are taken from the data.json file found at utils/data.json
     * @param tokenUri
     * @param clientId
     * @param clientSecret
     * @param username
     * @param password
     * @param scope
     * @return AccessToken
     * @throws JSONException
     */
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
        logger.info("Oauth Token for user {} with type {} is {}", username, tokenType, accessToken);
        return accessToken;
    }

    /**
     * This method performs a post as an attempt to add points code already added
     * @param accessToken
     */
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

    /**
     * This method performs a get call to the end point that returns the customer balance
     * @param accessToken
     */

    public void getCustomerPointsBalance(String accessToken) {

        Response response = given().auth().preemptive().oauth2(accessToken)
                .contentType("application/json")
                .when()
                .get(businessUrl + "api/private/manager/customer/balance");
        String responseBody = response.getBody().asString();
        if (responseBody.contains("accumulatedPoints") || response.getStatusCode() == 200){
            logger.info("Successful Response = " + responseBody);
        } else {
            logger.error("UnSuccessful Response = = {}", responseBody);
        }
    }

    //The method below i not being currently used
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
}

