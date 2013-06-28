package sqlite;

/**
 * Created by Borja on 27/06/13.
 */
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Borja on 1/06/13.
 */
public class DBManager extends SQLiteOpenHelper {
    final String _CREATE = "CREATE TABLE PROYECTOS (id INTEGER PRIMARY KEY, nombre TEXT, descripcion, TEXT, owner INTEGER)";
    final String _CREATE_2 = "CREATE TABLE USUARIOS (id INTEGER PRIMARY KEY, nombre TEXT, apellidos TEXT, telefono TEXT, mail TEXT)";
    final String _CREATE_3 = "CREATE TABLE USER_PROJ (id INTEGER PRIMARY KEY, id_usuario INTEGER, id_proyecto, INTEGER)";
    final String _CREATE_4= "CREATE TABLE USER_PROJ (id INTEGER PRIMARY KEY, id_usuario INTEGER, id_proyecto, INTEGER)";

    public DBManager(Context contexto, String nombre, SQLiteDatabase.CursorFactory factory, int version){

        super(contexto, nombre, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        try{
            sqLiteDatabase.execSQL(_CREATE);
            sqLiteDatabase.execSQL(_CREATE_2);
            sqLiteDatabase.execSQL(_CREATE_3);
            sqLiteDatabase.execSQL(_CREATE_4);
            System.out.print("CREATED");
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i2) {
        try{
            //sqLiteDatabase.execSQL("DROP TABLE IF EXISTS NOTAS");
            sqLiteDatabase.execSQL(_CREATE_2);
            System.out.print("UPDATED");
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
