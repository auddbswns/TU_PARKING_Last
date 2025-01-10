package com.example.tu_parking

import android.R
import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity


// 패키지 이름을 현재 프로젝트 패키지에 맞게 수정


class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    // 기본 생성자 삭제

    override fun onCreate(db: SQLiteDatabase) {
        // User 테이블 생성
        db.execSQL("CREATE TABLE User (car_num TEXT PRIMARY KEY)")

        // Parking 테이블 생성
        db.execSQL("CREATE TABLE Parking (parking_id TEXT PRIMARY KEY)")

        // Parking_spot 테이블 생성
        db.execSQL(
            """
            CREATE TABLE Parking_spot (
                spot_id INTEGER PRIMARY KEY AUTOINCREMENT,
                parking_id TEXT,
                spot_number TEXT,
                status INTEGER,
                FOREIGN KEY(parking_id) REFERENCES Parking(parking_id)
            )
        """.trimIndent()
        )

        // Log 테이블 생성
        db.execSQL(
            """
            CREATE TABLE Log (
                log_id INTEGER PRIMARY KEY AUTOINCREMENT,
                car_num TEXT,
                event_time TEXT,
                spot_id INTEGER,
                FOREIGN KEY(car_num) REFERENCES User(car_num),
                FOREIGN KEY(spot_id) REFERENCES Parking_spot(spot_id)
            )
        """.trimIndent()
        )



        // 기본 데이터 삽입///////////////////////////////////////////////////////////////////////////////////////////
        db.execSQL("INSERT INTO Parking (parking_id) VALUES ('한국공대주차장')")
        db.execSQL("INSERT INTO Parking (parking_id) VALUES ('와우리주차장')")
        db.execSQL("INSERT INTO Parking (parking_id) VALUES ('안녕동주차장')")
        //주차장은 한국공대만 쓸거고 자리는 세자리만
        db.execSQL("INSERT INTO Parking_spot (parking_id, spot_number, status) VALUES ('한국공대주차장', '1', 0)")
        db.execSQL("INSERT INTO Parking_spot (parking_id, spot_number, status) VALUES ('한국공대주차장', '2', 0)")
        db.execSQL("INSERT INTO Parking_spot (parking_id, spot_number, status) VALUES ('한국공대주차장', '3', 0)")
    }




    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS User")
        db.execSQL("DROP TABLE IF EXISTS Parking")
        db.execSQL("DROP TABLE IF EXISTS Parking_spot")
        db.execSQL("DROP TABLE IF EXISTS Log")
        onCreate(db)
    }

    companion object {
        private const val DATABASE_NAME = "ParkingSystem.db"
        private const val DATABASE_VERSION = 1
    }

    fun isCarParked(inputCarNum: String): Boolean { // 주차되어있는지 확인하는 코드
        val db = this.readableDatabase
        val query = """
        SELECT spot_id
        FROM Log
        WHERE car_num = ?
        ORDER BY event_time DESC
        LIMIT 1;
    """.trimIndent()

        val cursor = db.rawQuery(query, arrayOf(inputCarNum))
        val isParked = if (cursor.moveToFirst()) {
            cursor.getInt(0) != 0
        } else {
            false
        }
        cursor.close()
        return isParked
    }
    // 주차되었을때 값 들어가는 함수
    fun insertLog(carNum: String, spotId: Int, eventTime: String) {
        val db = this.writableDatabase

        // 로그테이블에 데이터 샆입
        val values = ContentValues().apply {
            put("car_num", carNum)  // 차량 번호
            put("spot_id", spotId)  // 주차된 spot_id 이거도 나중에 값 받아와서 자리값으로 넣어야지
            put("event_time", eventTime)  // 주차 시간
        }

        // Log 테이블에 데이터 삽입
        db.insert("Log", null, values)


        //그리고 나서 그 자리에 해당하는 status 1로 업뎃
        val updateValues = ContentValues().apply {
            put("status", 1)  // 1은 주차된 상태를 나타냄
        }

        // 주차된 spot의 status를 1로 업데이트
        db.update(
            "Parking_spot",
            updateValues,
            "spot_id = ?",
            arrayOf(spotId.toString())
        )
        db.close()
    }

    fun getLogsByCarNum(carNum: String): List<LogData> {
        val db = this.readableDatabase
        val query = """
        SELECT log_id, car_num, event_time, spot_id
        FROM Log
        WHERE car_num = ?
        ORDER BY event_time ASC
    """
        val cursor = db.rawQuery(query, arrayOf(carNum))
        val logs = mutableListOf<LogData>()

        if (cursor.moveToFirst()) {
            do {
                val logId = cursor.getInt(cursor.getColumnIndexOrThrow("log_id"))
                val eventTime = cursor.getString(cursor.getColumnIndexOrThrow("event_time"))
                val spotId = cursor.getInt(cursor.getColumnIndexOrThrow("spot_id"))
                logs.add(LogData(logId, carNum, eventTime, spotId))
            } while (cursor.moveToNext())
        }
        cursor.close()
        return logs
    }

    data class LogData( //로그에서 데이터 가져온걸 객체화
        val logId: Int,        // 로그 ID
        val carNum: String,    // 차량 번호
        val eventTime: String, // 이벤트 시간
        val spotId: Int        // 주차 구역 ID
    )

    // 남은자리르 세주는 메소드
    fun getAvailableSpots(parkingId: String): Int {
        val db = this.readableDatabase
        val query = """
        SELECT COUNT(*) 
        FROM Parking_spot 
        WHERE parking_id = ? AND status = 0
    """

        val cursor = db.rawQuery(query, arrayOf(parkingId)) //rawQuery는 쿼리 실행하는거임
        val availableSpots = if (cursor.moveToFirst()) {
            cursor.getInt(0)  // 결과로 나온 개수
        } else {
            0
        }
        cursor.close()
        return availableSpots
    }

    // 값 삭제
    fun resetDatabase() {
        val db = writableDatabase
        db.execSQL("DROP TABLE IF EXISTS User")
        db.execSQL("DROP TABLE IF EXISTS Parking")
        db.execSQL("DROP TABLE IF EXISTS Parking_spot")
        db.execSQL("DROP TABLE IF EXISTS Log")
        onCreate(db)
    }





}
