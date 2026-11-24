package com.example.historialmedico.ui.perfil

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.historialmedico.data.PerfilDao
import com.example.historialmedico.ui.components.AvisoPrivacidadScreen
import com.example.historialmedico.util.ReglasTexto
import com.example.historialmedico.util.ReglasTexto.CampoTexto
import kotlinx.coroutines.launch

@Composable
fun CrearPerfilScreen(perfilDao: PerfilDao, modifier: Modifier = Modifier) {
    //rememberSaveable: lo escrito sigue ahi si Android recrea la pantalla, por ejemplo al cambiar el modo oscuro
    var nombre by rememberSaveable { mutableStateOf("") }
    var registrarTitular by rememberSaveable { mutableStateOf(true) }
    var error by rememberSaveable { mutableStateOf<String?>(null) }
    var leyendoAviso by rememberSaveable { mutableStateOf(false) }
    //este no se guarda: si la app se reinicia a mitad de guardar, el boton no debe quedar bloqueado
    var guardando by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    //el aviso tapa el formulario, lo escrito se mantiene porque los estados estan arriba
    if (leyendoAviso) {
        AvisoPrivacidadScreen(onVolver = { leyendoAviso = false }, modifier = modifier)
        return
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            "Cree su perfil",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Text(
            "Es el primer paso. El perfil identifica a quien usa este dispositivo.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 8.dp, bottom = 24.dp)
        )
        OutlinedTextField(
            value = nombre,
            onValueChange = { if (it.length <= CampoTexto.NOMBRE_PERSONA.maximo) nombre = it },
            label = { Text("Su nombre") },
            supportingText = { Text("${nombre.length}/${CampoTexto.NOMBRE_PERSONA.maximo}") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))
        //toda la fila se puede tocar y el lector de pantalla la lee como un interruptor con su texto
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 48.dp)
                .toggleable(
                    value = registrarTitular,
                    role = Role.Switch,
                    onValueChange = { registrarTitular = it }
                ),
            verticalAlignment = Alignment.CenterVertically
        ) {
            //sin onCheckedChange: el toque lo recibe la fila entera
            Switch(checked = registrarTitular, onCheckedChange = null)
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                "También quiero registrar mi propia salud",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.weight(1f)
            )
        }
        Text(
            "Si solo cuida a otras personas, desactívelo. Podrá agregar pacientes después.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp)
        )
        error?.let {
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                it,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        //se muestra antes de guardar el primer dato
        Text(
            "Sus datos de salud se guardan solo en este teléfono. Al continuar, acepta el aviso de privacidad.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.fillMaxWidth()
        )
        TextButton(onClick = { leyendoAviso = true }) { Text("Leer aviso de privacidad") }
        Spacer(modifier = Modifier.height(8.dp))
        Button(
            enabled = !guardando,
            onClick = {
                val nombreLimpio = ReglasTexto.normalizar(nombre)
                val errorNombre = ReglasTexto.validar(nombre, CampoTexto.NOMBRE_PERSONA)
                if (errorNombre != null) {
                    error = errorNombre
                } else {
                    error = null
                    guardando = true
                    scope.launch {
                        try {
                            perfilDao.crearPerfil(nombreLimpio, registrarTitular)
                        } finally {
                            guardando = false
                        }
                    }
                }
            },
            //heightIn y no height: con letra grande el boton crece y el texto no se corta
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 56.dp)
        ) {
            Text("Continuar")
        }
    }
}