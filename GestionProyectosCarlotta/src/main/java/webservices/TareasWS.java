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

import models.Tareas;
import models.Usuario;

/**
 * Created by Borja on 28/06/13.
 */
public class TareasWS {
    String urlServer;
    public TareasWS(String urlServer){
        this.urlServer = urlServer;
    }
    public ArrayList<Tareas> getAllTareas(){
        ArrayList<Tareas> tareas = new ArrayList<Tareas>();
        //Obtener datos del webservice
        HttpClient httpClient = new DefaultHttpClient();
        HttpGet get = new HttpGet(urlServer+"gestProyectos.php?func=2");
        try{
            HttpResponse respuesta = httpClient.execute(get);
            String jsonData = EntityUtils.toString(respuesta.getEntity());
            //Obtener datos del webservice END//
            //Extraer y generar array a partir de JSON
            JSONArray jsonArray = new JSONArray(jsonData);
            for(int a=0;a<jsonArray.length();a++){
                JSONObject jUsuario = jsonArray.getJSONObject(a);
                Tareas tarea = new Tareas();
                //Dar propiedades al usuario
                tarea.setId(jUsuario.getInt("id"));
                tarea.setNombre(jUsuario.getString("nombre"));
                tarea.setProyecto(jUsuario.getInt("id_proyecto"));
                tarea.setDescripcion(jUsuario.getString("descripcion"));
                tarea.setCoste(jUsuario.getInt("coste"));
                tarea.setCosteFinal(jUsuario.getInt("costeFinal"));
                tarea.setValor(jUsuario.getInt("valor"));
                tarea.setCompletado(jUsuario.getInt("completado"));
                tarea.setUsuario(jUsuario.getInt("id_usuario"));
                tareas.add(tarea);
            }
            //Extraer y generar array a partir de JSON END//
        }catch(Exception e){
            e.printStackTrace();
            return null;
        }
        return tareas;
    }
    public boolean setTaskDone(int task){
        HttpClient client = new DefaultHttpClient();
        HttpGet get = new HttpGet(urlServer+"gestProyectos.php?func=3&taskId="+task);
        try {
            HttpResponse respuesta = client.execute(get);
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
        return true;
    }
    public boolean setTaskUnDone(int task){
        HttpClient client = new DefaultHttpClient();
        HttpGet get = new HttpGet(urlServer+"gestProyectos.php?func=4&taskId="+task);
        try {
            HttpResponse respuesta = client.execute(get);
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
        return true;
    }
    public boolean addTask(String nombre, String descripcion, int valor, int coste, int horas, int usuario, int proyecto){
        boolean result = false;
        // Create a new HttpClient and Post Header
        HttpClient httpclient = new DefaultHttpClient();
        HttpPost httppost = new HttpPost(urlServer+"gestProyectos.php?func=7");

        try {
            // Add your data
            List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(7);
            nameValuePairs.add(new BasicNameValuePair("nombre", nombre));
            nameValuePairs.add(new BasicNameValuePair("descripcion", descripcion));
            nameValuePairs.add(new BasicNameValuePair("valor", Integer.toString(valor)));
            nameValuePairs.add(new BasicNameValuePair("coste", Integer.toString(coste)));
            nameValuePairs.add(new BasicNameValuePair("horas", Integer.toString(coste)));
            nameValuePairs.add(new BasicNameValuePair("usuario", Integer.toString(usuario)));
            nameValuePairs.add(new BasicNameValuePair("proyecto", Integer.toString(proyecto)));
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
    public boolean addOverCost(int horas, int taskId){
        boolean result = false;
        // Create a new HttpClient and Post Header
        HttpClient httpclient = new DefaultHttpClient();
        HttpPost httppost = new HttpPost(urlServer+"gestProyectos.php?func=8");

        try {
            // Add your data
            List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
            nameValuePairs.add(new BasicNameValuePair("taskID", Integer.toString(taskId)));
            nameValuePairs.add(new BasicNameValuePair("coste", Integer.toString(horas)));
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
    public boolean deleteTask(int taskID){
        boolean result = false;
        // Create a new HttpClient and Post Header
        HttpClient httpclient = new DefaultHttpClient();
        HttpPost httppost = new HttpPost(urlServer+"gestProyectos.php?func=9");

        try {
            // Add your data
            List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(1);
            nameValuePairs.add(new BasicNameValuePair("taskID", Integer.toString(taskID)));
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
