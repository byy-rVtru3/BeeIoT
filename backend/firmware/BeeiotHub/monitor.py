"""
Simple serial monitor for ESP32
Reads output from serial port and displays it
"""
import serial
import sys

PORT = "COM5"
BAUDRATE = 115200

print(f"Connecting to {PORT} at {BAUDRATE} baud...")
print("Press Ctrl+C to exit\n")
print("="*60)

try:
    ser = serial.Serial(PORT, BAUDRATE, timeout=1)

    while True:
        if ser.in_waiting:
            try:
                line = ser.readline().decode('utf-8', errors='ignore')
                print(line, end='')
            except Exception as e:
                print(f"\n[MONITOR ERROR] {e}")

except KeyboardInterrupt:
    print("\n\nMonitoring stopped by user")
    sys.exit(0)

except Exception as e:
    print(f"\n[ERROR] {e}")
    print(f"\nMake sure:")
    print(f"  1. ESP32 is connected to {PORT}")
    print(f"  2. No other program is using the port")
    print(f"  3. Install pyserial: pip install pyserial")
    sys.exit(1)

