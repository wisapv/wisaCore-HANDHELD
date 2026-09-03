package com.example.wisahandheld.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.wisahandheld.ui.components.BackButton
import com.example.wisahandheld.ui.theme.*

data class ScanResult(
    val kbn: String,
    val address: String,
    val partName: String,
    val qty: Int
)

@Composable
fun InputStockScreen(
    zoneCode: String,
    addressCode: String,
    scan: ScanResult,
    onNotFound: () -> Unit,
    onBack: () -> Unit,
    onSend: (String,String,String) -> Unit
) {

    var box by remember { mutableStateOf("") }
    var pcs by remember { mutableStateOf("") }
    var seq by remember { mutableStateOf("1") }

    Box(
        Modifier
            .fillMaxSize()
            .background(Canvas)
    ) {

        Column(
            Modifier
                .fillMaxSize()
                .padding(20.dp)
        ) {

            Text("$zoneCode · $addressCode", color=Muted, fontSize=9.sp)
            Text("Input Stock", color=Ink, fontSize=14.sp, fontWeight=FontWeight.Medium)

            Spacer(Modifier.height(14.dp))

            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement=Arrangement.spacedBy(8.dp)
            ){
                ScannedField("KBN",scan.kbn,Modifier.weight(1f))
                ScannedField("ADDRESS",scan.address,Modifier.weight(1f))
            }

            Spacer(Modifier.height(8.dp))

            ScannedField("PART NAME",scan.partName)

            Spacer(Modifier.height(8.dp))

            ScannedField("Q'TY","${scan.qty}")

            Spacer(Modifier.height(12.dp))

            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement=Arrangement.spacedBy(7.dp)
            ){
                EditableField("BOX",box,{box=it},Modifier.weight(1f))
                EditableField("PCS",pcs,{pcs=it},Modifier.weight(1f))
                EditableField("SEQ",seq,{seq=it},Modifier.weight(1f))
            }

            Spacer(Modifier.weight(1f))

            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement=Arrangement.End
            ){

                Row(
                    Modifier
                        .width(110.dp)
                        .border(1.dp,ErrorText.copy(.25f),RoundedCornerShape(11.dp))
                        .clickable{onNotFound()}
                        .padding(vertical=12.dp),
                    horizontalArrangement=Arrangement.Center
                ){
                    Text("Not Found",color=ErrorText,fontSize=10.sp)
                }

                Spacer(Modifier.width(8.dp))

                Row(
                    Modifier
                        .width(110.dp)
                        .background(Ink,RoundedCornerShape(11.dp))
                        .clickable{onSend(box,pcs,seq)}
                        .padding(vertical=12.dp),
                    horizontalArrangement=Arrangement.Center
                ){
                    Text("Send",color=Lemon,fontSize=10.sp)
                }
            }
        }


        BackButton(
            onClick = onBack,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(20.dp)
        )
    }
}


@Composable
private fun ScannedField(
    label:String,
    value:String,
    modifier:Modifier=Modifier
){
    Column(modifier){
        FieldLabel(label,Lemon)

        Box(
            Modifier
                .fillMaxWidth()
                .background(CardWhite,RoundedCornerShape(11.dp))
                .border(1.dp,BorderLight,RoundedCornerShape(11.dp))
                .padding(12.dp)
        ){
            Text(value,color=Ink,fontSize=11.sp)
        }
    }
}


@Composable
private fun EditableField(
    label:String,
    value:String,
    onValueChange:(String)->Unit,
    modifier:Modifier=Modifier
){
    Column(modifier){

        FieldLabel(label,ManualAccent)

        Box(
            Modifier
                .fillMaxWidth()
                .background(CardWhite,RoundedCornerShape(11.dp))
                .border(1.dp,BorderLight,RoundedCornerShape(11.dp))
                .padding(10.dp),
            contentAlignment=Alignment.Center
        ){
            BasicTextField(
                value=value,
                onValueChange=onValueChange,
                singleLine=true,
                textStyle=TextStyle(
                    color=Ink,
                    fontSize=12.sp,
                    fontWeight=FontWeight.Bold,
                    textAlign=TextAlign.Center
                ),
                cursorBrush=SolidColor(Ink)
            )
        }
    }
}


@Composable
private fun FieldLabel(
    text:String,
    dotColor:Color
){
    Row(
        verticalAlignment=Alignment.CenterVertically,
        modifier=Modifier.padding(bottom=4.dp)
    ){
        Box(
            Modifier
                .size(6.dp)
                .background(dotColor,CircleShape)
        )

        Spacer(Modifier.width(4.dp))

        Text(text,color=Muted,fontSize=8.sp)
    }
}