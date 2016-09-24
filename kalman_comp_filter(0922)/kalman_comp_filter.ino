/* 칼만 필터 부분 출처 저작권
 *  Copyright (C) 2012 Kristian Lauszus, TKJ Electronics. All rights reserved.
  This software may be distributed and modified under the terms of the GNU
  General Public License version 2 (GPL2) as published by the Free Software
  Foundation and appearing in the file GPL2.TXT included in the packaging of
  this file. Please note that GPL2 Section 2[b] requires that all works based
  on this software must also be made publicly available under the terms of
  the GPL2 ("Copyleft").
  Contact information
  -------------------
  Kristian Lauszus, TKJ Electronics
  Web : http://www.tkjelectronics.com
  e-mail : kristianl@tkjelectronics.com
*/

#include <Wire.h>
#include <I2Cdev.h>
#include <MPU6050.h>
#include "Kalman.h" // Source: https://github.com/TKJElectronics/KalmanFilter

//2fsr_blue
#include <SoftwareSerial.h>
SoftwareSerial bt(7,8);//tx,rx

Kalman kalmanX; // Create the Kalman instances
Kalman kalmanY;

//code_can
//압력센서 변수
int fsr_f=A0;
int fsr_b=A2;
int fsr_Value_f;
int fsr_Value_b;

/* IMU Data */
int16_t accX, accY, accZ;
int16_t tempRaw;
int16_t gyroX, gyroY, gyroZ;

double accXangle, accYangle; // Angle calculate using the accelerometer
double temp; // Temperature
double gyroXangle, gyroYangle; // Angle calculate using the gyro
double compAngleX, compAngleY; // Calculate the angle using a complementary filter
double kalAngleX, kalAngleY; // Calculate the angle using a Kalman filter

int int_kalAngleX, int_kalAngleY; //정수 타입
int x=10000, y=10000; //비교값 기준 (초기값)
int count=0, last_stat=0;

uint32_t timer;
uint8_t i2cData[14]; // Buffer for I2C data
////////////////////////////

void setup() {
  Serial.begin(9600);
  Wire.begin();

  //2fsr_blue
  bt.begin(9600);
  
  i2cData[0] = 7; // Set the sample rate to 1000Hz - 8kHz/(7+1) = 1000Hz
  i2cData[1] = 0x00; // Disable FSYNC and set 260 Hz Acc filtering, 256 Hz Gyro filtering, 8 KHz sampling
  i2cData[2] = 0x00; // Set Gyro Full Scale Range to ±250deg/s
  i2cData[3] = 0x00; // Set Accelerometer Full Scale Range to ±2g
  while (i2cWrite(0x19, i2cData, 4, false)); // Write to all four registers at once
  while (i2cWrite(0x6B, 0x01, true)); // PLL with X axis gyroscope reference and disable sleep mode
  while (i2cRead(0x75, i2cData, 1));
  if (i2cData[0] != 0x68) { // Read "WHO_AM_I" register
    Serial.print(F("Error reading sensor"));
    while (1);

  }
  
  int time=0;

  delay(100); // Wait for sensor to stabilize

  /* Set kalman and gyro starting angle */
  while (i2cRead(0x3B, i2cData, 6));
  accX = ((i2cData[0] << 8) | i2cData[1]);
  accY = ((i2cData[2] << 8) | i2cData[3]);
  accZ = ((i2cData[4] << 8) | i2cData[5]);
  // atan2 outputs the value of -π to π (radians) - see http://en.wikipedia.org/wiki/Atan2
  // We then convert it to 0 to 2π and then from radians to degrees
  accYangle = (atan2(accX, accZ) + PI) * RAD_TO_DEG;
  accXangle = (atan2(accY, accZ) + PI) * RAD_TO_DEG;

  kalmanX.setAngle(accXangle); // Set starting angle
  kalmanY.setAngle(accYangle);
  gyroXangle = accXangle;
  gyroYangle = accYangle;
  compAngleX = accXangle;
  compAngleY = accYangle;

  timer = micros();

}

void loop() {
  angleValue();

  //code_can(start)
  //변수 추가: 센서를 측정할 때마다 변하는 값들...
String send=""; 

//센서값 읽기
fsr_Value_f=analogRead(fsr_f);
fsr_Value_b=analogRead(fsr_b);

Serial.print("count: ");
Serial.println(count);

if(fsr_Value_f==0 && fsr_Value_b==0) {  //압력센서가 모두 0인 경우 
//x또는 y 값 차이가 3보다 클 경우만 출력 (왜냐하면 가만히 있을 때 값이 1~2정도 튈 때가 있기 때문)
if( (abs(x-int_kalAngleX)>3) || (abs(y-int_kalAngleY)>3) ) { //움직이는 중
  int stat=1; //공중에서 흔들고 있는 상황!!
  int num;
  num = abs((x+y) - (int_kalAngleX + int_kalAngleY));
  x = int_kalAngleX;
  y = int_kalAngleY;
 
  if((stat==last_stat)&&(num>=6)) { //이전에도 흔들고 있는 상황이었나?
    count++;
    if(count>180) {
      send=send+"qdz";
      bt.print(send);
      Serial.println(send);
      count=0;
    }
   }
  last_stat=1;
 }
}
else {
  last_stat=0;
  count=0;
//앞 쪽 압력과 뒤쪽 압력의 값 차이가 일정 크기 이상 차이날 경우
//압력값이 큰 값만 전송
  if(abs(fsr_Value_f-fsr_Value_b)>730) {
    if(fsr_Value_f>820) { //앞 누를경우 820
      send=send+"qf"+String(fsr_Value_f);
      send=send+"z";
      Serial.println("Fronttttttttttttttt.");
    }
    else if(fsr_Value_b>750) { //뒤 누를 경우 850
      send=send+"qb"+String(fsr_Value_b);
      send=send+"z";
      Serial.println("Backkkkkkkkkkkkkkkk.");
    }
  }

bt.print(send);

//시리얼로 확인
Serial.println(send);

delay(2000);
}

//code_can(end)

}

void angleValue() {
  /* Update all the values */
  while (i2cRead(0x3B, i2cData, 14));
  accX = ((i2cData[0] << 8) | i2cData[1]);
  accY = ((i2cData[2] << 8) | i2cData[3]);
  accZ = ((i2cData[4] << 8) | i2cData[5]);
  tempRaw = ((i2cData[6] << 8) | i2cData[7]);
  gyroX = ((i2cData[8] << 8) | i2cData[9]);
  gyroY = ((i2cData[10] << 8) | i2cData[11]);
  gyroZ = ((i2cData[12] << 8) | i2cData[13]);

  // atan2 outputs the value of -π to π (radians) - see http://en.wikipedia.org/wiki/Atan2
  // We then convert it to 0 to 2π and then from radians to degrees
  accXangle = (atan2(accY, accZ) + PI) * RAD_TO_DEG;
  accYangle = (atan2(accX, accZ) + PI) * RAD_TO_DEG;

  double gyroXrate = (double)gyroX / 131.0;
  double gyroYrate = -((double)gyroY / 131.0);
  gyroXangle += gyroXrate * ((double)(micros() - timer) / 1000000); // Calculate gyro angle without any filter
  gyroYangle += gyroYrate * ((double)(micros() - timer) / 1000000);


  compAngleX = (0.93 * (compAngleX + (gyroXrate * (double)(micros() - timer) / 1000000))) + (0.07 * accXangle); // Calculate the angle using a Complimentary filter
  compAngleY = (0.93 * (compAngleY + (gyroYrate * (double)(micros() - timer) / 1000000))) + (0.07 * accYangle);

  kalAngleX = kalmanX.getAngle(accXangle, gyroXrate, (double)(micros() - timer) / 1000000); // Calculate the angle using a Kalman filter
  kalAngleY = kalmanY.getAngle(accYangle, gyroYrate, (double)(micros() - timer) / 1000000);
  int_kalAngleX = (int)kalAngleX;
  int_kalAngleY = (int)kalAngleY;

  if((x==10000) && (y==10000)) {
    x = int_kalAngleX;
    y = int_kalAngleY;
  }
  
  timer = micros();

  temp = ((double)tempRaw + 12412.0) / 340.0;


}




