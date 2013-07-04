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

import carlotta.digital.gestionproyectoscarlotta.R;
import models.Proyecto;
import models.Usuario;

/**
 * Created by Borja on 25/06/13.
 */
public class ProyectosWS {
    String urlServer;
    public ProyectosWS(String urlServer){
        this.urlServer = urlServer;
    }
    public ArrayList<Proyecto> getAllProyectos(){
        ArrayList<Proyecto> proyectos = new ArrayList<Proyecto>();
        //Obtener datos del webservice
        HttpClient httpClient = new DefaultHttpClient();
        HttpGet get = new HttpGet(urlServer+"gestProyectos.php?func=1");
        try{
            HttpResponse respuesta = httpClient.execute(get);
            String jsonData = EntityUtils.toString(respuesta.getEntity());
            //Obtener datos del webservice END//
            //Extraer y generar array a partir de JSON
            JSONArray jsonArray = new JSONArray(jsonData);
            for(int a=0;a<jsonArray.length();a++){
                JSONObject jProyecto = jsonArray.getJSONObject(a);
                JSONObject jOwner = jProyecto.getJSONObject("owner");
                Usuario usuario = new Usuario();
                //Dar propiedades al usuario
                usuario.setId(jOwner.getInt("id"));
                usuario.setNombre(jOwner.getString("nombre"));
                usuario.setApellidos(jOwner.getString("apellido"));
                usuario.setMail(jOwner.getString("mail"));
                //Dar propiedades al proyecto
                Proyecto proyecto = new Proyecto();
                proyecto.setId(jProyecto.getInt("id"));
                proyecto.setNombre(jProyecto.getString("nombre"));
                proyecto.setDescripcion(jProyecto.getString("descripcion"));
                proyecto.setOwner(usuario.getId());
                //Añadir el proyeto a la lista de proyectos y continuar
                proyectos.add(proyecto);
            }
            //Extraer y generar array a partir de JSON END//
        }catch(Exception e){
            e.printStackTrace();
            return null;
        }
        return proyectos;
    }
    public Proyecto getProyectoById(int id){
        Proyecto proyecto = new Proyecto();

        return proyecto;
    }
    public boolean deleteProject(int id){
        //Obtener datos del webservice
        HttpClient httpClient = new DefaultHttpClient();
        HttpGet get = new HttpGet(urlServer+"gestProyectos.php?func=5&projectID="+id);
        try{
            HttpResponse respuesta = httpClient.execute(get);
        }catch(Exception e){
            e.printStackTrace();
        }
        return true;
    }
    public boolean createProject(int owner, String nombre, String descripcion){
        boolean result = false;
        // Create a new HttpClient and Post Header
        HttpClient httpclient = new DefaultHttpClient();
        HttpPost httppost = new HttpPost(urlServer+"gestProyectos.php?func=6");

        try {
            // Add your data
            List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(3);
            nameValuePairs.add(new BasicNameValuePair("nombre", nombre));
            nameValuePairs.add(new BasicNameValuePair("desc", descripcion));
            nameValuePairs.add(new BasicNameValuePair("owner", Integer.toString(owner)));
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
