package kaptan;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.usb.UsbConst;
import javax.usb.UsbControlIrp;
import javax.usb.UsbDevice;
import javax.usb.UsbDeviceDescriptor;
import javax.usb.UsbDisconnectedException;
import javax.usb.UsbException;
import javax.usb.UsbHostManager;
import javax.usb.UsbHub;
import javax.usb.UsbServices;

public class Kaptan {

	private Logger log = LogFactory.getLog(Kaptan.class);

	private final static short VENDOR = 9408;
	private final static short PRODUCT = 3;
	private final static byte REQUEST = 0x01;
	private final static short VALUE = 0x0100;
	private final static short INDEX = 0;

	private static final short REPORT_1 = 1;
	private static final short REPORT_2 = 2;

	public static void main(String[] args) throws Exception {
		LogConfig logConfig = new LogConfig();
		logConfig.setLevel(LogLevel.DEBUG);
		LogFactory.configure(logConfig);
		new Kaptan().execute();
	}

	String[] DIRECTIONS = { "NNW", "NW", "WNW", "W", "WSW", "SW", "SSW", "S", "SSE", "SE", "ESE", "E", "ENE", "NE",
			"NNE", "N" };

	public void execute() throws Exception {
		UsbServices services;
		try {
			services = UsbHostManager.getUsbServices();
		} catch (Exception e) {
			log.log(Level.SEVERE, "Could not retrieve list of USB services", e);
			return;
		}
		UsbHub rootHub;
		try {
			rootHub = services.getRootUsbHub();
		} catch (Exception e) {
			log.log(Level.SEVERE, "Could not get root USB hub", e);
			return;
		}
		UsbDevice device = findDevice(rootHub, VENDOR, PRODUCT);
		if (device == null) {
			log.warning("Could not find USB device, make sure it is plugged in and try again");
			return;
		}
		String product = "unknown";
		try {
			product = device.getProductString();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (UsbDisconnectedException e) {
			e.printStackTrace();
		} catch (UsbException e) {
			e.printStackTrace();
		}
		log.log(Level.FINE, "Found USB device: {0}", product);
		while (true) {
			fetch(device);
			Thread.sleep(1000);
		}
	}

	private void fetch(UsbDevice device) throws IllegalArgumentException, UsbDisconnectedException, UsbException {
		short value = VALUE + REPORT_1;
		byte[] data = new byte[50];
		UsbControlIrp irp = device.createUsbControlIrp((byte) (UsbConst.REQUESTTYPE_TYPE_CLASS
				| UsbConst.REQUESTTYPE_RECIPIENT_INTERFACE | UsbConst.ENDPOINT_DIRECTION_IN), REQUEST, value, INDEX);
		irp.setData(data);
		irp.setLength(50);
		if (data == null || data.length == 0) {
			log.warning("No data received");
		}
		device.syncSubmit(irp);
		log.info("Length=" + irp.getActualLength());
		if (!irp.isComplete()) {
			log.fine("IRP is not complete");
		}
		if (irp.isUsbException()) {
			log.log(Level.SEVERE, "Could not get data", irp.getUsbException());
			return;
		}
		decode(Arrays.copyOfRange(data, 1, data.length - 1));
	}

	@SuppressWarnings("unchecked")
	public UsbDevice findDevice(UsbHub hub, short vendorId, short productId) {
		for (UsbDevice device : (List<UsbDevice>) hub.getAttachedUsbDevices()) {
			UsbDeviceDescriptor desc = device.getUsbDeviceDescriptor();
			if (desc.idVendor() == vendorId && desc.idProduct() == productId)
				return device;
			if (device.isUsbHub()) {
				device = findDevice((UsbHub) device, vendorId, productId);
				if (device != null)
					return device;
			}
		}
		return null;
	}

	private WeatherData decode(byte[] data) {
		WeatherData weatherData = new WeatherData();
		weatherData.setTime(Calendar.getInstance());
		int messageId = data[2] & 0x0f;
		if (messageId == 1) {
			weatherData.setWindSpeed(getWindSpeed(data));
			weatherData.setWindDirection(DIRECTIONS[getWindDirection(data)]);
			weatherData.setRain(getRain(data));
			log.log(Level.INFO, "speed={0} rain={1} direction={2}",
					new Object[] { weatherData.getWindSpeed(), weatherData.getRain(), weatherData.getWindDirection() });

		}
		if (messageId == 8) {
			weatherData.setWindSpeed(getWindSpeed(data));
			weatherData.setTemperature(getTemperature(data));
			weatherData.setHumidity(getHumidity(data));
			log.log(Level.INFO, "speed={0} temp={1} humidity={2}", new Object[] { weatherData.getWindSpeed(),
					weatherData.getTemperature(), weatherData.getHumidity() });

		}
		return weatherData;
	}

	/**
	 * @return Wind speed in kilometers per hour (kph)
	 */
	private double getWindSpeed(byte[] data) {
		int leftSide = (data[3] & 0x1f) << 3;
		int rightSide = (data[4] & 0x70) >> 4;
		return leftSide | rightSide;
	}

	private int getWindDirection(byte[] data) {
		return data[4] & 0x0f;
	}

	private int getHumidity(byte[] data) {
		return data[6] & 0x7f;
	}

	private double getTemperature(byte[] data) {
		// This item spans bytes, have to reconstruct it
		int leftSide = (data[4] & 0x0f) << 7;
		int rightSide = data[5] & 0x7f;
		float combined = leftSide | rightSide;
		return (combined - 400) / 10.0;
	}

	private double getRain(byte[] data) {
		int tips = ((data[5] & 0x3f) << 7) | (data[6] & 0x7f);
		return 0.01 * tips;
	}
}
