package com.example.tu_parking

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.tu_parking.databinding.ActivitySeatBinding

class SeatActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySeatBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySeatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val dbHelper = DatabaseHelper(this)
        val parkingId = "한국공대주차장"

        val availableSpots = dbHelper.getAvailableSpots(parkingId)
        binding.seatView.setText("남은자리 $availableSpots/3")
    }

    override fun onResume() {
        super.onResume()
        // DB에서 남은 자리 개수를 다시 가져와서 업데이트
        val dbHelper = DatabaseHelper(this)
        val parkingId = "한국공대주차장"
        val availableSpots = dbHelper.getAvailableSpots(parkingId)

        // 남은 자리를 화면에 갱신
        binding.seatView.setText("남은자리 $availableSpots/3")
    }
}
