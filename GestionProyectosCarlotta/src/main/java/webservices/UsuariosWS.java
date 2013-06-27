package webservices;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

import models.Proyecto;
import models.Usuario;

/**
 * Created by Borja on 27/06/13.
 */
public class UsuariosWS {
    String urlServer;
    public UsuariosWS(String urlServer){
        this.urlServer = urlServer;
    }
    public ArrayList<Usuario> getAllUsuarios(){
        ArrayList<Usuario> usuarios = new ArrayList<Usuario>();
        //Obtener datos del webservice
        HttpClient httpClient = new DefaultHttpClient();
        HttpGet get = new HttpGet(urlServer+"gestUsuarios.php?func=1");
        try{
            HttpResponse respuesta = httpClient.execute(get);
            String jsonData = EntityUtils.toString(respuesta.getEntity());
            //Obtener datos del webservice END//
            //Extraer y generar array a partir de JSON
            JSONArray jsonArray = new JSONArray(jsonData);
            for(int a=0;a<jsonArray.length();a++){
                JSONObject jUsuario = jsonArray.getJSONObject(a);
                Usuario usuario = new Usuario();
                //Dar propiedades al usuario
                usuario.setId(jUsuario.getInt("id"));
                usuario.setApellidos(jUsuario.getString("apellido"));
                usuario.setNombre(jUsuario.getString("nombre"));
                usuario.setTelefono(jUsuario.getInt("telefono"));
                usuario.setMail(jUsuario.getString("mail"));
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

}
