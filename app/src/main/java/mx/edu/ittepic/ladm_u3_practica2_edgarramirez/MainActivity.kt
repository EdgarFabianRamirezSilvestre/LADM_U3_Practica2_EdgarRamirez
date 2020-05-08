package mx.edu.ittepic.ladm_u3_practica2_edgarramirez

import android.app.Dialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AlertDialog
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_main.cantidad
import kotlinx.android.synthetic.main.activity_main.celular
import kotlinx.android.synthetic.main.activity_main.descripcion
import kotlinx.android.synthetic.main.activity_main.entregado
import kotlinx.android.synthetic.main.activity_main.nombre
import kotlinx.android.synthetic.main.activity_main.domicilio
import kotlinx.android.synthetic.main.activity_main.precio

class MainActivity : AppCompatActivity() {

    var baseRemota = FirebaseFirestore.getInstance()
    var listaID = ArrayList<String>()
    var dataLista =ArrayList<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        consultar.setOnClickListener {
            construirDialogo()
        }

        insertar.setOnClickListener {
            insertarRegistro()
        }

        baseRemota.collection("restaurante")
                .addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                    if(firebaseFirestoreException != null){
                        //si es diferente de null entonces si hay error y entra al if
                        Toast.makeText(this,"ERRROR NO SE PUEDE ACCEDER A CONSULTA",Toast.LENGTH_LONG).show()
                        return@addSnapshotListener
                    }

                    dataLista.clear()
                    listaID.clear()
                    for(document in querySnapshot!!){
                        var cadena ="Nombre: "+document.getString("nombre") + "\n"+"Cel: "+document.getString("celular") +
                                "\nDomicilio: "+ document.getString("domicilio")
                        dataLista.add(cadena)
                        listaID.add(document.id)
                    }
                    if(dataLista.size==0){
                        dataLista.add("NO HAY DATOS")
                    }
                    var adaptador =
                            ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,dataLista)
                    lista.adapter=adaptador
                }

        lista.setOnItemClickListener { parent, view, position, id ->
            if(listaID.size==0){
                return@setOnItemClickListener
            }
            AlertaElminarActualizar(position)
        }
    }//onCreate

    private fun llamarVentanaActualizar(idActualizar: String) {
        baseRemota.collection("restaurante")
                .document(idActualizar)
                .get()
                .addOnSuccessListener {
                    var v = Intent(this,Main2Activity :: class.java)
                    v.putExtra("id",idActualizar)
                    v.putExtra("nombre",it.getString("nombre"))
                    v.putExtra("domicilio",it.getString("domicilio"))
                    v.putExtra("celular",it.getString("celular"))
                    v.putExtra("producto",it.getString("pedido.producto"))
                    v.putExtra("precio",it.getDouble("pedido.precio")!!.toDouble())
                    v.putExtra("cantidad",it.getLong("pedido.cantidad"))
                    v.putExtra("entregado",it.getBoolean("pedido.entregado"))

                    startActivity(v)
                }
                .addOnFailureListener {
                    Toast.makeText(this,"ERROR NO HAY CONEXION",Toast.LENGTH_LONG).show()
                }
    }//llamarVentanaActualizar

    private fun AlertaElminarActualizar(position: Int) {
        AlertDialog.Builder(this).setTitle("ATENCION").setMessage("¿QUE DESEAS REALIZAR CON EL CLIENTE? \n${dataLista[position]}")
            .setPositiveButton("ELIMINAR"){d,w->
                eliminar(listaID[position])
            }
            .setNegativeButton("ACTUALIZAR"){d,w->
                llamarVentanaActualizar(listaID[position])


            }
            .setNeutralButton("CANCELAR"){d,w->
            }
            .show()
    }//AlertaElminarActualizar

    private fun construirDialogo() {
        var dialogo =Dialog(this)
        dialogo.setContentView(R.layout.activity_consultas)
        //Objetos de interaccion
        var valor =dialogo.findViewById<EditText>(R.id.valor)
        var posicion = dialogo.findViewById<Spinner>(R.id.clave)
        var buscar = dialogo.findViewById<Button>(R.id.buscar)
        var cerrar = dialogo.findViewById<Button>(R.id.cerrar)

        dialogo.show()

        cerrar.setOnClickListener { dialogo.dismiss() }
        buscar.setOnClickListener {
            if(valor.text.isEmpty()){
                Toast.makeText(this,"SE REQUIERE UN VALOR PARA REALIZAR LA BUSQUEDA",Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }
            realizarConsulta(valor.text.toString(), posicion.selectedItemPosition)
            dialogo.dismiss()
        }
    }//construirDialogo

    private fun eliminar(idEliminar: String) {
        baseRemota.collection("restaurante")
            .document(idEliminar)
            .delete()
            .addOnSuccessListener {
                Toast.makeText(this,"SE ELIMINO CON EXITO",Toast.LENGTH_LONG).show()
            }
            .addOnFailureListener {
                Toast.makeText(this,"ERROR AL ELIMINAR",Toast.LENGTH_LONG).show()
            }
    }//eliminar

    private fun realizarConsulta(valor: String, clave: Int) {
        when(clave){
            0->{consultaNombre(valor)}
            1->{consultaCelular(valor)}
            2->{consultaDomicilio(valor)}
            3->{consultaPedido(valor)}
            4->{consultaPrecio(valor.toFloat())}
            5->{consultaCantidad(valor.toInt())}
        }
    }//realizarConsulta

    private fun consultaPedido(valor: String) {
        dataLista.clear()
        listaID.clear()
        baseRemota.collection("restaurante")
                .whereEqualTo("pedido.producto", valor.toString())
                .addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                    if (firebaseFirestoreException != null) {
                        return@addSnapshotListener
                    }
                    var res = ""
                    for (document in querySnapshot!!) {
                        res += "ID: " + document.id + "\nNombre: " + document.getString("nombre") +
                                "\nDomicilio: " + document.getString("domicilio") +
                                "\nCelular: " + document.getString("celular") +
                                "\nProducto: " + document.get("pedido.producto") +
                                "\nPrecio: " + document.get("pedido.precio") +
                                "\nCantidad: " + document.get("pedido.cantidad") +
                                "\nEntregado: " + document.get("pedido.entregado")+"\n\n"
                        dataLista.add(res)
                        listaID.add(document.id)
                    }
                }
    }//consultaPedido

    private fun consultaPrecio(valor: Float) {
        dataLista.clear()
        listaID.clear()
        baseRemota.collection("restaurante")
            .whereEqualTo("pedido.precio", valor.toDouble())
            .addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                if (firebaseFirestoreException != null) {
                    return@addSnapshotListener
                }
                var res = ""
                for (document in querySnapshot!!) {
                    res += "ID: " + document.id + "\nNombre: " + document.getString("nombre") +
                            "\nDomicilio: " + document.getString("domicilio") +
                            "\nCelular: " + document.getString("celular") +
                            "\nProducto: " + document.get("pedido.producto") +
                            "\nPrecio: " + document.get("pedido.precio") +
                            "\nCantidad: " + document.get("pedido.cantidad") +
                            "\nEntregado: " + document.get("pedido.entregado")+"\n\n"
                    dataLista.add(res)
                    listaID.add(document.id)
                }
            }
    }//consultaPrecio

    private fun consultaDomicilio(valor: String) {
        dataLista.clear()
        listaID.clear()
        baseRemota.collection("restaurante")
                .whereEqualTo("domicilio", valor.toString())
                .addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                    if (firebaseFirestoreException != null) {
                        return@addSnapshotListener
                    }
                    var res = ""
                    for (document in querySnapshot!!) {
                        res += "ID: " + document.id + "\nNombre: " + document.getString("nombre") +
                                "\nDomicilio: " + document.getString("domicilio") +
                                "\nCelular: " + document.getString("celular") +
                                "\nProducto: " + document.get("pedido.producto") +
                                "\nPrecio: " + document.get("pedido.precio") +
                                "\nCantidad: " + document.get("pedido.cantidad") +
                                "\nEntregado: " + document.get("pedido.entregado")+"\n\n"
                        dataLista.add(res)
                        listaID.add(document.id)
                    }
                }
    }//consultaDomicilio

    private fun consultaCelular(valor: String) {
        dataLista.clear()
        listaID.clear()
        baseRemota.collection("restaurante")
                .whereEqualTo("celular", valor.toString())
                .addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                    if (firebaseFirestoreException != null) {
                        return@addSnapshotListener
                    }
                    var res = ""
                    for (document in querySnapshot!!) {
                        res += "ID: " + document.id + "\nNombre: " + document.getString("nombre") +
                                "\nDomicilio: " + document.getString("domicilio") +
                                "\nCelular: " + document.getString("celular") +
                                "\nProducto: " + document.get("pedido.producto") +
                                "\nPrecio: " + document.get("pedido.precio") +
                                "\nCantidad: " + document.get("pedido.cantidad") +
                                "\nEntregado: " + document.get("pedido.entregado")+"\n\n"
                        dataLista.add(res)
                        listaID.add(document.id)
                    }
                }
    }//consultaCelular

    private fun consultaCantidad(valor: Int) {
        dataLista.clear()
        listaID.clear()
        baseRemota.collection("restaurante")
                .whereLessThanOrEqualTo("pedido.cantidad", valor.toInt())
                .addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                    if (firebaseFirestoreException != null) {
                        return@addSnapshotListener
                    }
                    var res = ""
                    for (document in querySnapshot!!) {
                        res += "ID: " + document.id + "\nNombre: " + document.getString("nombre") +
                                "\nDomicilio: " + document.getString("domicilio") +
                                "\nCelular: " + document.getString("celular") +
                                "\nProducto: " + document.get("pedido.producto") +
                                "\nPrecio: " + document.get("pedido.precio") +
                                "\nCantidad: " + document.get("pedido.cantidad") +
                                "\nEntregado: " + document.get("pedido.entregado")+"\n\n"
                        dataLista.add(res)
                        listaID.add(document.id)

                    }
                }
    }//consultaCantidad

    private fun consultaNombre(valor: String) {
        dataLista.clear()
        listaID.clear()

        baseRemota.collection("restaurante")
            .whereEqualTo("nombre", valor.toString())
            .addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                if (firebaseFirestoreException != null) {
                    return@addSnapshotListener
                }
                var res = ""
                for (document in querySnapshot!!) {
                    res += "ID: " + document.id + "\nNombre: " + document.getString("nombre") +
                            "\nDomicilio: " + document.getString("domicilio") +
                            "\nCelular: " + document.getString("celular") +
                            "\nProducto: " + document.get("pedido.producto") +
                            "\nPrecio: " + document.get("pedido.precio") +
                            "\nCantidad: " + document.get("pedido.cantidad") +
                            "\nEntregado: " + document.get("pedido.entregado")+"\n\n"
                    dataLista.add(res)
                    listaID.add(document.id)
                }
            }
    }//consultaNombre

    private fun insertarRegistro() {
        var datosInsertar = hashMapOf(
                "nombre" to nombre.text.toString(),
                "domicilio" to domicilio.text.toString(),
                "celular" to celular.text.toString(),
                "pedido" to hashMapOf(
                        "producto" to descripcion.text.toString(),
                        "precio" to precio.text.toString().toFloat(),
                        "cantidad" to cantidad.text.toString().toInt(),
                        "entregado" to entregado.isChecked
                )

        )
        baseRemota.collection("restaurante").add(datosInsertar as Map<String, Any>)
                .addOnSuccessListener {
                    Toast.makeText(this,"DATOS INSERTADOS CORRECTAMENTE", Toast.LENGTH_LONG).show()
                    nombre.setText(""); domicilio.setText(""); celular.setText("");descripcion.setText(""); precio.setText("");cantidad.setText("");
                }
                .addOnFailureListener {
                    Toast.makeText(this,"ERROR , NO SE REALIZO LA INSERCIÓN",Toast.LENGTH_LONG).show()
                }
    }//insertarRegistro

}//MainActivity