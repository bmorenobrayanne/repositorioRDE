package com.example.rdekids.iu.adapter


import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.rdekids.R
import com.example.rdekids.local.entities.Puntaje

class PartidasAdapter(
    private val partidas: MutableList<Puntaje>,
    private val onEliminarClick: ((Puntaje) -> Unit)? = null
) : RecyclerView.Adapter<PartidasAdapter.PartidaViewHolder>() {

    inner class PartidaViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val itemUsuario: TextView = view.findViewById(R.id.itemUsuario)
        val itemPuntaje: TextView = view.findViewById(R.id.itemPuntaje)
        val itemFecha: TextView = view.findViewById(R.id.itemFecha)
        val btnEliminar: TextView = view.findViewById(R.id.btnEliminar)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PartidaViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_tarea, parent, false)
        return PartidaViewHolder(view)
    }

    override fun onBindViewHolder(holder: PartidaViewHolder, position: Int) {
        val p = partidas[position]

        holder.itemUsuario.text = "Usuario: ${p.usuario}"
        holder.itemPuntaje.text = "Puntaje: ${p.puntaje}"
        holder.itemFecha.text = "Fecha: ${p.fecha}"

        // Botón de eliminar
        holder.btnEliminar.setOnClickListener {
            // Llamamos al callback si existe
            onEliminarClick?.invoke(p)
        }
    }

    override fun getItemCount(): Int = partidas.size

    /**
     * Actualiza la lista de partidas en el adapter y notifica cambios.
     * Esto permite que LiveData o el ViewModel actualicen la UI fácilmente.
     */
    fun actualizarLista(nuevaLista: List<Puntaje>) {
        partidas.clear()
        partidas.addAll(nuevaLista)
        notifyDataSetChanged()
    }
}