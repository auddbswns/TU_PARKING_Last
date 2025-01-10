package com.example.tu_parking

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ImageView
import android.widget.Spinner
import android.widget.TableLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class ParkingZone : AppCompatActivity() {

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_parking_zone)

        val spinner: Spinner = findViewById(R.id.spin)
        val parking1: TableLayout = findViewById(R.id.parking1)
        val parking2: TableLayout = findViewById(R.id.parking2)
        val parking3: TableLayout = findViewById(R.id.parking3)
        val remainingSpotsTextView: TextView = findViewById(R.id.jare) // 남은자리 TextView 참조

        val dbHelper = DatabaseHelper(this)

        // 항목 배열 정의
        val items = arrayOf("주차장을 선택하시오", "한국공대주차장", "주차장2", "주차장3")

        // 어댑터 생성
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, items)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter

        // 항목 선택 리스너 설정
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                val selectedItem = parent.getItemAtPosition(position).toString()

                // 선택된 항목에 대한 작업 수행
                if (selectedItem == "주차장을 선택하시오") {
                    parking1.visibility = View.GONE
                    parking2.visibility = View.GONE
                    parking3.visibility = View.GONE
                    remainingSpotsTextView.visibility = View.GONE // 남은 자리 숨김
                } else { // 선택한 주차장에 대한 실행
                    val availableSpots = dbHelper.getAvailableSpots(selectedItem) // 선택한 주차장의 남은 자리 가져오기
                    remainingSpotsTextView.text = "남은자리: $availableSpots/3" // 남은 자리 업데이트
                    remainingSpotsTextView.visibility = View.VISIBLE // 남은 자리 보이기

                    when (selectedItem) {
                        "한국공대주차장" -> {
                            parking1.visibility = View.VISIBLE
                            parking2.visibility = View.GONE
                            parking3.visibility = View.GONE
                        }
                        "주차장2" -> {
                            parking1.visibility = View.GONE
                            parking2.visibility = View.VISIBLE
                            parking3.visibility = View.GONE
                        }
                        "주차장3" -> {
                            parking1.visibility = View.GONE
                            parking2.visibility = View.GONE
                            parking3.visibility = View.VISIBLE
                        }
                    }
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // 아무것도 선택되지 않았을 때의 처리
            }
        }

        val Cbtn : ImageView = findViewById(R.id.Cbtn)

        Cbtn.setOnClickListener{
            finish()
        }

    }
}
