package com.example.tu_parking

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.RequestQueue

import com.android.volley.toolbox.Volley
import com.example.tu_parking.databinding.ActivitySearchBinding
import com.google.android.material.color.utilities.SchemeTonalSpot
import com.google.gson.annotations.SerializedName
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Retrofit
import retrofit2.Response
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET

data class ParkingData(
    @SerializedName("carNum")
    val carNum: String,

    @SerializedName("spotid")
    val spotid: Int,

    @SerializedName("eventTime")
    val eventTime: String
)


interface RetrofitService {

    // GET 요청 예제: Flask 서버에서 주차 데이터를 받아오는 API 호출
    @GET("get_data")
    fun getParkingData(): Call<ParkingData>
}



class SearchActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val binding = ActivitySearchBinding.inflate(layoutInflater)

        setContentView(binding.root)


        var edit = binding.number.editableText

        val dbHelper = DatabaseHelper(this)

        // 데이터베이스 초기화
        dbHelper.resetDatabase()


        // Retrofit 설정
        val retrofit = Retrofit.Builder()
            .baseUrl("http://192.168.0.27:5000/")  // Flask 서버의 URL
            .addConverterFactory(GsonConverterFactory.create())  // Gson Converter 사용
            .build()

        val service = retrofit.create(RetrofitService::class.java)

        // API 요청
        service.getParkingData().enqueue(object : Callback<ParkingData> {
            override fun onResponse(call: Call<ParkingData>, response: Response<ParkingData>) {
                if (response.isSuccessful) {
                    val parkingData = response.body()
                    Log.d("SearchActivity", "Response Success: $parkingData")

                    // 받은 데이터를 데이터베이스에 저장
                    parkingData?.let {
                        dbHelper.insertLog(it.carNum, it.spotid, it.eventTime)
                    }
                } else {
                    Log.d("SearchActivity", "Response Failure: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<ParkingData>, t: Throwable) {
                Log.d("SearchActivity", "Failure: ${t.message}")
            }
        })

        // 차량 번호가 맞을 경우 MyCarActivity로 이동
        binding.btn.setOnClickListener {

            val editText = binding.number.editableText.toString()


            // 차량 번호로 주차 여부 확인
            if (dbHelper.isCarParked(editText)) {
                // 차량이 주차 중인 경우
                Toast.makeText(this, "차량이 주차되어 있습니다!", Toast.LENGTH_SHORT).show()

                // MyCarActivity로 이동
                val intent = Intent(this, MycarActivity::class.java)
                intent.putExtra("car_num", editText)  // 번호판 정보 넘기기
                startActivity(intent)
            } else {
                // 차량이 주차되지 않은 경우
                Toast.makeText(this, "차량이 주차되어 있지 않습니다.", Toast.LENGTH_SHORT).show()
            }
        }
        binding.btn2.setOnClickListener{
            var intent = Intent(this, ParkingZone::class.java)
            startActivity(intent)
        }


    }
}