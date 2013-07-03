package webservices;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Borja on 3/07/13.
 */
public class LoginWS {
    String urlServer;
    public LoginWS(String urlServer){
        this.urlServer = urlServer;
    }
    public int doLogin(String mail, String password){
        int result=0;
        // Create a new HttpClient and Post Header
        HttpClient httpclient = new DefaultHttpClient();
        HttpPost httppost = new HttpPost(urlServer+"gestLogin.php");

        try {
            // Add your data
            List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
            nameValuePairs.add(new BasicNameValuePair("mail", mail));
            nameValuePairs.add(new BasicNameValuePair("pass", password));
            httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

            // Execute HTTP Post Request
            HttpResponse response = httpclient.execute(httppost);
            String json = EntityUtils.toString(response.getEntity());
            if(json!=null){
                JSONObject object = new JSONObject(json);
                result = object.getInt("result");
            }else{
                result = 0;
            }
        } catch (ClientProtocolException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            result = 0;
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            result = 0;
        }
        catch (JSONException je){
            je.printStackTrace();
            result = 0;
        }
        return result;
    }
}
