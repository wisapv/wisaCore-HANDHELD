package com.example.wisahandheld.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.wisahandheld.ui.components.BackButton
import com.example.wisahandheld.ui.theme.*

data class KbnRow(
    val supplier: String,
    val kbn: String,
    val address: String
)

@Composable
fun AddressDetailScreen(
    zoneCode:String,
    addressCode:String,
    remain:Int,
    rows:List<KbnRow>,
    onSelectRow:(KbnRow)->Unit,
    onBack:()->Unit,
    onEdit:()->Unit
){

    Box(
        Modifier
            .fillMaxSize()
            .background(Canvas)
    ){

        Column(
            Modifier
                .fillMaxSize()
                .padding(20.dp)
        ){

            Text("$zoneCode · $addressCode",color=Muted,fontSize=9.sp)
            Text("Address",color=Ink,fontSize=15.sp,fontWeight=FontWeight.Medium)

            Spacer(Modifier.height(10.dp))


            Row(
                Modifier
                    .fillMaxWidth()
                    .background(Ink,RoundedCornerShape(13.dp))
                    .padding(15.dp),
                horizontalArrangement=Arrangement.SpaceBetween,
                verticalAlignment=Alignment.CenterVertically
            ){
                Text("REMAIN",color=CardWhite.copy(.55f),fontSize=9.sp)
                Text("$remain",color=Lemon,fontSize=20.sp)
            }


            Spacer(Modifier.height(10.dp))


            LazyColumn(
                Modifier
                    .weight(1f)
                    .padding(bottom=70.dp),
                verticalArrangement=Arrangement.spacedBy(8.dp)
            ){

                items(rows){ row ->

                    Row(
                        Modifier
                            .fillMaxWidth()
                            .background(CardWhite,RoundedCornerShape(11.dp))
                            .border(1.dp,BorderLight,RoundedCornerShape(11.dp))
                            .clickable{onSelectRow(row)}
                            .padding(12.dp),

                        horizontalArrangement=Arrangement.SpaceBetween,
                        verticalAlignment=Alignment.CenterVertically
                    ){

                        Column {

                            Text(
                                row.supplier,
                                color=Ink,
                                fontSize=11.sp,
                                fontWeight=FontWeight.Medium
                            )

                            Text(
                                row.address,
                                color=Muted,
                                fontSize=9.sp
                            )
                        }


                        Text(
                            row.kbn,
                            color=LemonBadgeText,
                            fontSize=9.sp,
                            modifier=Modifier
                                .background(LemonSoft,RoundedCornerShape(7.dp))
                                .padding(horizontal=9.dp,vertical=3.dp)
                        )

                    }
                }
            }


            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement=Arrangement.End
            ){

                Row(
                    Modifier
                        .width(110.dp)
                        .background(Ink,RoundedCornerShape(12.dp))
                        .clickable{onEdit()}
                        .padding(vertical=13.dp),
                    horizontalArrangement=Arrangement.Center
                ){

                    Text(
                        "Edit",
                        color=Lemon,
                        fontSize=12.sp
                    )
                }
            }
        }


        BackButton(
            onClick=onBack,
            modifier=Modifier
                .align(Alignment.BottomStart)
                .padding(20.dp)
        )

    }
}