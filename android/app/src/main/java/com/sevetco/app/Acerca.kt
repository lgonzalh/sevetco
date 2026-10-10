package com.sevetco.app

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Pantalla "Acerca de...".
 *
 * Misma informacion, estilo y estructura que la pantalla Acerca de... de la app
 * Pixelados, con los colores de SEVETCO.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PantallaAcerca(onVolver: () -> Unit) {
    val contexto = LocalContext.current

    fun abrir(url: String) {
        runCatching {
            contexto.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
        }
    }

    val version = remember {
        try {
            contexto.packageManager
                .getPackageInfo(contexto.packageName, 0)
                .versionName ?: "1.0"
        } catch (e: Exception) {
            "1.0"
        }
    }

    Scaffold(
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(AzulApp)
                    .padding(vertical = 12.dp, horizontal = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onVolver) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Volver",
                        tint = Color.White
                    )
                }
                Text(
                    "Acerca de esta App...",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        },
        containerColor = FondoApp
    ) { relleno ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(relleno)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(R.drawable.luis),
                contentDescription = "Luis Gonzalez",
                modifier = Modifier.size(120.dp).clip(RoundedCornerShape(24.dp))
            )
            Spacer(Modifier.height(14.dp))
            Text(
                "Luis Gonzalez",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = AzulApp
            )
            Text(
                "AI Engineer | Programmer | Entrepreneur",
                style = MaterialTheme.typography.titleSmall,
                color = GrisApp
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "Version $version",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = AzulApp
            )
            Spacer(Modifier.height(18.dp))

            TarjetaAcerca {
                Parrafo(
                    "Soy Luis Gonzalez, AI Engineer especializado en disenar herramientas y " +
                        "plataformas que combinan inteligencia humana e inteligencia artificial " +
                        "para ampliar las capacidades de los equipos de desarrollo."
                )
                Spacer(Modifier.height(10.dp))
                Parrafo(
                    "Construyo plataformas B2B, Micro-SaaS y herramientas de ingenieria de " +
                        "software principalmente sobre .NET/C#, arquitecturas web y serverless, " +
                        "con especial interes en sistemas local-first y aplicaciones donde la IA " +
                        "puede trabajar directamente sobre el entorno y los datos del usuario."
                )
                Spacer(Modifier.height(10.dp))
                Parrafo(
                    "La IA no deberia limitarse a generar codigo: debe comprender el proyecto, " +
                        "conservar su contexto y participar en su evolucion de forma trazable. Ese " +
                        "principio esta en accion en herramientas como Condor y en las soluciones " +
                        "empresariales de Lantonium para automatizar cargas masivas de datos."
                )
            }

            Spacer(Modifier.height(20.dp))

            TarjetaAcerca {
                Text(
                    "Pila tecnologica",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = AzulApp
                )
                FlowRow(
                    modifier = Modifier.padding(top = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(
                        ".NET C#", "ASP.NET Core", "PostgreSQL", "Supabase", "Kotlin",
                        "Android SDK", "Python", "Ollama", "SignalR", "Git", "Firebase",
                        "Google Cloud Run"
                    ).forEach { etiqueta ->
                        SuggestionChip(
                            onClick = {},
                            label = { Text(etiqueta, fontSize = 13.sp) },
                            shape = RoundedCornerShape(10.dp),
                            colors = SuggestionChipDefaults.suggestionChipColors(
                                containerColor = AzulClaroApp,
                                labelColor = Color(0xFF1A1A1A)
                            ),
                            border = SuggestionChipDefaults.suggestionChipBorder(
                                enabled = true,
                                borderColor = BordeApp
                            )
                        )
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            TarjetaAcerca {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "Contacto",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = AzulApp
                    )
                    Spacer(Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconoContacto(R.drawable.lantonium_logo_black, "Sitio web", Modifier.weight(1f)) {
                            abrir("https://lantonium.com/acerca")
                        }
                        IconoContacto(R.drawable.github, "GitHub", Modifier.weight(1f)) {
                            abrir("https://github.com/lgonzalh")
                        }
                        IconoContacto(R.drawable.linkedin, "LinkedIn", Modifier.weight(1f)) {
                            abrir("https://www.linkedin.com/in/lantonium/")
                        }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconoContacto(R.drawable.whatsapp, "WhatsApp", Modifier.weight(1f)) {
                            abrir("https://wa.me/573246864991")
                        }
                        IconoContacto(R.drawable.outlook, "Email", Modifier.weight(1f)) {
                            abrir("mailto:lgonzalh@outlook.com")
                        }
                        Spacer(Modifier.weight(1f))
                    }
                    Spacer(Modifier.height(10.dp))
                    Text(
                        "Si quieres conversar sobre un proyecto que tengas en mente, o " +
                            "simplemente intercambiar ideas sobre ingenieria de software, puedes " +
                            "escribirme a lgonzalh@outlook.com o conectarte conmigo en LinkedIn " +
                            "o GitHub o por WhatsApp.",
                        style = MaterialTheme.typography.bodySmall,
                        color = GrisApp,
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(Modifier.height(24.dp))
            Text(
                "\u00a9 2021\u20132026 Luis Gonzalez. Todos los derechos reservados.",
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF9AA6BC),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun TarjetaAcerca(contenido: @Composable () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(18.dp)) { contenido() }
    }
}

@Composable
private fun Parrafo(texto: String) {
    Text(
        texto,
        style = MaterialTheme.typography.bodyMedium,
        lineHeight = 22.sp,
        color = Color(0xFF1A1A1A)
    )
}

@Composable
private fun IconoContacto(
    recurso: Int,
    etiqueta: String,
    modificador: Modifier = Modifier,
    onClick: () -> Unit
) {
    Column(
        modifier = modificador.padding(6.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Button(
            onClick = onClick,
            modifier = Modifier.size(56.dp),
            shape = RoundedCornerShape(16.dp),
            contentPadding = PaddingValues(10.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.White),
            border = BorderStroke(1.dp, BordeApp)
        ) {
            Image(
                painter = painterResource(recurso),
                contentDescription = etiqueta,
                modifier = Modifier.fillMaxSize()
            )
        }
        Spacer(Modifier.height(4.dp))
        Text(etiqueta, style = MaterialTheme.typography.labelSmall, color = Color(0xFF1A1A1A))
    }
}

// Colores de SEVETCO
private val AzulApp = Color(0xFF1F3864)
private val AzulClaroApp = Color(0xFFEAEEF7)
private val GrisApp = Color(0xFF6B7280)
private val BordeApp = Color(0xFFDFE3EA)
private val FondoApp = Color(0xFFF4F6FA)

