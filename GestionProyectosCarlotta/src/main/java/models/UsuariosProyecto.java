package models;

/**
 * Created by Borja on 25/06/13.
 */
public class UsuariosProyecto {
    int id;
    int usuario;
    int proyecto;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUsuario() {
        return usuario;
    }

    public void setUsuario(int usuario) {
        this.usuario = usuario;
    }

    public int getProyecto() {
        return proyecto;
    }

    public void setProyecto(int proyecto) {
        this.proyecto = proyecto;
    }
}
