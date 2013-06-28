package webservices;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

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
}
