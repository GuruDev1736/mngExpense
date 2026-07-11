package com.convodoc.app.Activities.Intro

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.Code
import androidx.compose.material.icons.rounded.Compress
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.FolderZip
import androidx.compose.material.icons.rounded.PictureAsPdf
import androidx.compose.material.icons.rounded.QrCodeScanner
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.SyncAlt
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.convodoc.app.Model.ToolItem
import com.convodoc.app.ui.theme.Base64Color
import com.convodoc.app.ui.theme.CompressColor
import com.convodoc.app.ui.theme.ConvertColor
import com.convodoc.app.ui.theme.ConvodocTheme
import com.convodoc.app.ui.theme.FileColor
import com.convodoc.app.ui.theme.PdfColor
import com.convodoc.app.ui.theme.QRColor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onToolClick: (ToolItem) -> Unit = {}
) {

    val tools = remember {
        listOf(
            ToolItem(
                1,
                "Image Compressor",
                "Reduce image size",
                Icons.Rounded.Compress,
                CompressColor
            ),
            ToolItem(
                2,
                "Image Converter",
                "Convert image formats",
                Icons.Rounded.SyncAlt,
                ConvertColor
            ),
            ToolItem(
                3,
                "PDF Tools",
                "Merge, Split & More",
                Icons.Rounded.PictureAsPdf,
                PdfColor
            ),
            ToolItem(
                4,
                "Base64",
                "Encode & Decode",
                Icons.Rounded.Code,
                Base64Color
            ),
            ToolItem(
                5,
                "QR Scanner",
                "Scan & Generate",
                Icons.Rounded.QrCodeScanner,
                QRColor
            ),
            ToolItem(
                6,
                "ZIP Tools",
                "Zip & Unzip",
                Icons.Rounded.FolderZip,
                FileColor
            )
        )
    }

    Scaffold(
        topBar = {

            CenterAlignedTopAppBar(

                title = {

                    Column {

                        Text(
                            text = "ConvoDoc",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )

                        Text(
                            text = "Your All-in-One File Toolkit",
                            style = MaterialTheme.typography.bodySmall
                        )

                    }

                },

                actions = {

                    IconButton(onClick = { }) {
                        Icon(
                            Icons.Rounded.Settings,
                            null
                        )
                    }

                }

            )

        }

    ) { padding ->

        LazyColumn(

            modifier = Modifier
                .fillMaxSize()
                .padding(padding)

        ) {

            item {

                Text(
                    text = "Quick Tools",
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

            }

            item {

                LazyVerticalGrid(

                    columns = GridCells.Fixed(2),
                    modifier = Modifier.height(540.dp),
                    userScrollEnabled = false,
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)

                ) {

                    items(tools) {

                        ToolCard(
                            tool = it,
                            onClick = {
                                onToolClick(it)
                            }
                        )

                    }

                }

            }

            item {

                Spacer(Modifier.height(24.dp))

                Text(
                    text = "Recent Files",
                    modifier = Modifier.padding(horizontal = 16.dp),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

            }

            items(3) {

                ListItem(

                    headlineContent = {

                        Text("Document_${it + 1}.pdf")

                    },

                    supportingContent = {

                        Text("2.5 MB")

                    },

                    leadingContent = {

                        Icon(
                            Icons.Rounded.Description,
                            null
                        )

                    }

                )

            }

            item {

                Spacer(Modifier.height(24.dp))

            }

        }

    }

}


@Composable
fun ToolCard(
    tool: ToolItem,
    onClick: () -> Unit
) {

    ElevatedCard(

        modifier = Modifier
            .fillMaxWidth()
            .height(170.dp),

        onClick = onClick,

        shape = RoundedCornerShape(20.dp)

    ) {

        Column(

            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),

            verticalArrangement = Arrangement.SpaceBetween

        ) {

            Box(

                modifier = Modifier
                    .size(52.dp)
                    .background(
                        tool.color.copy(alpha = .15f),
                        CircleShape
                    ),

                contentAlignment = Alignment.Center

            ) {

                Icon(
                    imageVector = tool.icon,
                    contentDescription = null,
                    tint = tool.color,
                    modifier = Modifier.size(28.dp)
                )

            }

            Column {

                Text(
                    text = tool.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(Modifier.height(4.dp))

                Text(
                    text = tool.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

            }

            Row(
                horizontalArrangement = Arrangement.End,
                modifier = Modifier.fillMaxWidth()
            ) {

                Icon(
                    Icons.AutoMirrored.Rounded.ArrowForward,
                    null,
                    tint = tool.color
                )

            }

        }

    }

}


@Preview(showBackground = true)
@Composable
fun HomePreview() {
    ConvodocTheme {
        HomeScreen { }
    }
}