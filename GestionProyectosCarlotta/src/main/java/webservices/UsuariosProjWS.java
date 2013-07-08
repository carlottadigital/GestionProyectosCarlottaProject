package webservices;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import models.Usuario;
import models.UsuariosProyecto;

/**
 * Created by Borja on 27/06/13.
 */
public class UsuariosProjWS {
    String urlServer;
    public UsuariosProjWS(String urlServer){
        this.urlServer = urlServer;
    }
    public ArrayList<UsuariosProyecto> getAllUsuarios(){
        ArrayList<UsuariosProyecto> usuarios = new ArrayList<UsuariosProyecto>();
        //Obtener datos del webservice
        HttpClient httpClient = new DefaultHttpClient();
        HttpGet get = new HttpGet(urlServer+"gestUsuarios.php?func=2");
        try{
            HttpResponse respuesta = httpClient.execute(get);
            String jsonData = EntityUtils.toString(respuesta.getEntity());
            //Obtener datos del webservice END//
            //Extraer y generar array a partir de JSON
            JSONArray jsonArray = new JSONArray(jsonData);
            for(int a=0;a<jsonArray.length();a++){
                JSONObject jUsuario = jsonArray.getJSONObject(a);
                UsuariosProyecto usuario = new UsuariosProyecto();
                //Dar propiedades al usuario
                usuario.setId(jUsuario.getInt("id"));
                usuario.setProyecto(jUsuario.getInt("proyectoID"));
                usuario.setUsuario(jUsuario.getInt("userID"));
                //Añadir el proyeto a la lista de proyectos y continuar
                usuarios.add(usuario);
            }
            //Extraer y generar array a partir de JSON END//
        }catch(Exception e){
            e.printStackTrace();
            return null;
        }
        return usuarios;
    }
    public boolean enrolUser(int idProyecto, int idUser){
        boolean result = false;
        // Create a new HttpClient and Post Header
        HttpClient httpclient = new DefaultHttpClient();
        HttpPost httppost = new HttpPost(urlServer+"gestUsuarios.php?func=5");

        try {
            // Add your data
            List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
            nameValuePairs.add(new BasicNameValuePair("proyecto", Integer.toString(idProyecto)));
            nameValuePairs.add(new BasicNameValuePair("usuario", Integer.toString(idUser)));
            httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

            // Execute HTTP Post Request
            HttpResponse response = httpclient.execute(httppost);
            result = true;
        } catch (ClientProtocolException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            result = false;
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            result = false;
        }
        return result;
    }
}
