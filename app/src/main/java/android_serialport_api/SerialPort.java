/*
 * Copyright 2009 Cedric Priscal
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License. 
 */

package android_serialport_api;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.util.Log;

// 시리얼 포트와의 통신을 위한 기능을 제공
public class SerialPort {

	private static final String TAG = "SerialPort";

	/*
	 * Do not remove or rename the field mFd: it is used by native method close();
	 */
	// 시리얼 포트 파일 디스크립터를 나타내는 변수
	// 파일 디스크립터(File Descriptor): 프로그램이 파일을 열거나 생성하고, 파일을 읽거나 쓰는 등의 입출력 작업을 수행할 때 사용
	private FileDescriptor mFd;
	// 시리얼 포트로부터 데이터를 읽기 위한 객체
	private FileInputStream mFileInputStream;
	// 시리얼 포트로 데이터를 쓰기 위한 객체
	private FileOutputStream mFileOutputStream;

	// 생성자 메서드로, 주어진 디바이스 파일과 통신 속도, 플래그를 기반으로 시리얼 포트를 열고 설정
	// 플래그를 기반: 플래그는 보통 논리적인 상태를 표현하는 데 사용되는 변수로, 일반적으로 불리언 형태로 구현됨
	// 시리얼 포트 디바이스 파일을 나타
	// baudrate는 통신 속도
	// flags는 통신 설정에 대한 플래그
	public SerialPort(File device, int baudrate, int flags) throws SecurityException, IOException {

		/* Check access permission */
		if (!device.canRead() || !device.canWrite()) {
			try {
				/* Missing read/write permission, trying to chmod the file */
				Process su;
				su = Runtime.getRuntime().exec("/system/bin/su");
				String cmd = "chmod 666 " + device.getAbsolutePath() + "\n"
						+ "exit\n";
				su.getOutputStream().write(cmd.getBytes());
				if ((su.waitFor() != 0) || !device.canRead()
						|| !device.canWrite()) {
					throw new SecurityException();
				}
			} catch (Exception e) {
				e.printStackTrace();
				throw new SecurityException();
			}
		}

		mFd = open(device.getAbsolutePath(), baudrate, flags);
		if (mFd == null) {
			Log.e(TAG, "native open returns null");
			throw new IOException();
		}
		mFileInputStream = new FileInputStream(mFd);
		mFileOutputStream = new FileOutputStream(mFd);
	}

	// Getters and setters
	// 객체를 반환하는 메서드로, 시리얼 포트로부터 데이터를 읽기 위해 사용
	public InputStream getInputStream() {
		return mFileInputStream;
	}

	// 객체를 반환하는 메서드로, 시리얼 포트로 데이터를 쓰기 위해 사용
	public OutputStream getOutputStream() {
		return mFileOutputStream;
	}

	// JNI (JNI는 자바 언어로 작성된 코드에서 C/C++ 코드를 호출하거나 C/C++ 코드에서 자바 언어로 작성된 코드를 호출할 수 있는 기술)을 통해 시리얼 포트를 열고 파일 디스크립터를 반환하는 네이티브 메서드
	private native static FileDescriptor open(String path, int baudrate, int flags);
	// 시리얼 포트를 닫는 네이티브 메서드
	public native void close();
	// serial_port 라이브러리를 로드
	static {
		System.loadLibrary("serial_port");
	}
}
