package edu.illinois.ncsa.isda.imagetools.ext.camera;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.illinois.ncsa.isda.imagetools.core.datatype.ImageObject;
import edu.illinois.ncsa.isda.imagetools.core.datatype.ImageObjectByte;
import edu.illinois.ncsa.isda.imagetools.core.datatype.ImageObjectDouble;
import edu.illinois.ncsa.isda.imagetools.core.datatype.ImageObjectUShort;

import javax.comm.CommPortIdentifier;
import javax.comm.SerialPort;
import javax.comm.UnsupportedCommOperationException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.util.Enumeration;


/**
 * Control the Indigo Omega camera using the serial port interface. This
 * implements the spec found in Omega Interface Control Document 102-0005-03
 * Version 130.
 * <p/>
 * To use the camera, first call findCamera(). This will try and locate the
 * Omega camera on the serialport. If one is found the camera object is
 * returned, if none is found an exception will be thrown. After working with
 * the camera the code is responsible for closing the camera using close().
 * <p/>
 * Once the camera is located the user can control the camera using the
 * functions in this class. Following shows how to grab a single frame from the
 * camera:
 * <pre>
 *   IndigoOmegaCamera camera = new IndigoOmegaCamera();
 *   camera.doFCC();
 *   image = camera.getFrame(IndigoOmegaCamera.DIGITAL_OUTPUT_8_BIT);
 *   camera.close();
 * </pre>
 * <p/>
 *
 * @author Rob Kooper
 * @version 1.0
 */
public class IndigoOmegaCamera extends Camera {
    // functions
    private static final byte NO_OP = 0x00;
    private static final byte SET_DEFAULTS = 0x01;
    private static final byte CAMERA_RESET = 0x02;
    private static final byte RESTORE_FACTORY_DEFAULTS = 0x03;
    private static final byte GET_SERIAL_NUMBER = 0x04;
    private static final byte GET_REVISION = 0x05;
    private static final byte GET_STATUS = 0x06;
    private static final byte DYN_RNG_CTRL_MODE = 0x0A;
    private static final byte FCC_MODE = 0x0B;
    private static final byte DO_FCC = 0x0C;
    private static final byte FCC_PERIOD = 0x0D;
    private static final byte FCC_TEMP_DELTA = 0x0E;
    private static final byte ANALOG_VIDEO_MODE = 0x0F;
    private static final byte POLARITY = 0x10;
    private static final byte IMAGE_ORIENTATION = 0x11;
    private static final byte DIGITAL_OUTPUT_MODE = 0x12;
    private static final byte IMAGE_OPTIMIZATION_MODE = 0x13;
    private static final byte CONTRAST = 0x14;
    private static final byte BRIGHTNESS = 0x15;
    private static final byte GET_CONSTRAST = 0x16;
    private static final byte GET_BRIGHTNESS = 0x17;
    private static final byte BRIGHTNESS_BIAS = 0x18;
    private static final byte GET_BRIGHTNESS_BIAS = 0x19;
    private static final byte SYMBOL_COLOR = 0x1A;
    private static final byte SET_LENS = 0x1E;
    private static final byte SPOT_METER_MODE = 0x1F;
    private static final byte GET_CASE_TEMP = 0x20;
    private static final byte ISOTHERM_MODE = 0x22;
    private static final byte SET_ISOTHERM_THRESHOLDS = 0x23;
    private static final byte GET_ISOTHERM_LIMITS = 0x24;
    private static final byte TEST_PATTERN_MODE = 0x25;
    private static final byte GET_FCC_PERIOD = 0x26;
    private static final byte GET_FCC_TEMP_DELTA = 0x27;
    private static final byte GET_LENS_ID = 0x28;
    private static final byte GET_CAMERA_OPTIONS = 0x29;
    private static final byte GET_SPOT_METER = 0x2A;
    private static final byte DOWNLOAD_ROW = (byte) 0x50;

    // dynamic range control modes
    public static final byte DYN_RNG_CTRL_AUTOMATIC = 0x00;
    public static final byte DYN_RNG_CTRL_LOW_GAIN = 0x01;
    public static final byte DYN_RNG_CTRL_HIGH_GAIN = 0x02;
    public static final byte DYN_RNG_CTRL_DISABLED = 0x03;

    // FCC mode
    public static final byte FCC_MANUAL = 0x00;
    public static final byte FCC_AUTOMATIC = 0x01;

    // analog video mode
    public static final byte ANALOG_VIDEO_REAL_TIME = 0;
    public static final byte ANALOG_VIDEO_FREEZE_FRAME = 1;

    // polarity
    public static final byte POLARITY_WHITE_HOT = 0x00;
    public static final byte POLARITY_BLACK_HOT = 0x01;

    // image orientation
    public static final byte IMAGE_ORIENTATION_NORMAL = 0x00;
    public static final byte IMAGE_ORIENTATION_REVERT = 0x02;

    // digital output mode
    public static final byte DIGITAL_OUTPUT_14_BIT = 0;
    public static final byte DIGITAL_OUTPUT_8_BIT = 1;

    // image optimization mode
    public static final byte IMAGE_OPTIMIZATION_AUTOMATIC = 0;
    public static final byte IMAGE_OPTIMIZATION_MANUAL_OPTIMIZED = 1;
    public static final byte IMAGE_OPTIMIZATION_AUTO_BRIGHTNESS = 2;
    public static final byte IMAGE_OPTIMIZATION_MANUAL_FIXED = 3;

    // symbol color
    public static final byte SYMBOL_COLOR_BLACK_WHITE = 0;
    public static final byte SYMBOL_COLOR_OVERBRIGHT = 1;
    public static final byte SYMBOL_COLOR_OFF = 2;

    // spot meter
    public static final byte SPOT_METER_DISABLED = 0;
    public static final byte SPOT_METER_ON_FAHRENHEIT = 1;
    public static final byte SPOT_METER_ON_CENTIGRADE = 3;

    // isotherm
    public static final byte ISOTHERM_DISABLED = 0;
    public static final byte ISOTHERM_ENABLED = 1;

    // test pattern
    public static final byte TEST_PATTERN_OFF = 0;
    public static final byte TEST_PATTERN_RAMP = 1;
    public static final byte TEST_PATTERN_SHADE = 2;

    // camera options
    public static final int OPTION_ISOTHERM = 0x0010;
    public static final int OPTION_SPOT_METER = 0x0008;
    public static final int OPTION_DIGITAL_OUTPUT = 0x0004;
    public static final int OPTION_AUTO_DYN_RNG = 0x0002;
    public static final int OPTION_EXTENDED_TEMP = 0x0001;

    // constants for case temperature
    public static final int KELVIN = 0;
    public static final int CENTIGRADE = 1;
    public static final int FAHRENHEIT = 2;

    // constants for frane temperature
    public static final int RAW = 3;

    // status byte definition
    private static final byte UL3_OK = 0x00;
    private static final byte UL3_BUSY = 0x01;
    private static final byte UL3_NOT_READY = 0x02;
    private static final byte UL3_RANGE_ERROR = 0x03;
    private static final byte UL3_CHECKSUM_ERROR = 0x04;
    private static final byte UL3_UNDEFINED_PROCESS_ERROR = 0x05;
    private static final byte UL3_UNDEFINED_FUNCTION_ERROR = 0x06;
    private static final byte UL3_TIMEOUT_ERROR = 0x07;
    private static final byte UL3_BYTE_COUNT_ERROR = 0x09;
    private static final byte UL3_FEATURE_NOT_ENABLED = 0x0A;

    // special byte packets begin with
    private static final byte MAGIC_BYTE = 0x6e;

    // variabled used with the camera
    private SerialPort serialPort = null;
    private InputStream input = null;
    private OutputStream output = null;

    private byte spot;
    private byte isotherm;
    private byte testPattern;
    private byte lens;
    //private byte sync;
    private byte digitalOutputMode;
    private byte symbolColor;
    private byte imageOrientation;
    private byte imageOptimization;
    private byte polarity;
    private byte analogVideoMode;
    private byte fccMode;
    private byte dynamicRangeControlMode;
    private int frameScale;

    // camera options
    private IndigoOmegaCameraOptions options;

    // serial number
    private int serialnumber;

    // conversion from raw to temperature
    // these are calculated using a series of meassurements
    static double[] conv_m = {0.04898657, 0.04898657, 0.08817583};
    static double[] conv_b = {-83.21919542, -356.36919542, -609.46455176};

    private static Log logger = LogFactory.getLog(IndigoOmegaCamera.class);

    /**
     * Try and locate an Indigo Omega camera. This constructor will loop through
     * all serial ports and try and find an Indigo Omega camera. If a camera is
     * found it will return an instance of the IndigoOmegaCamera which allows to
     * control the camera. After using the camrea, the application is
     * responsible for closing the camera using close().
     *
     * @throws IOException if no camera could be found.
     */
    public IndigoOmegaCamera() throws IOException {
        Enumeration portList = CommPortIdentifier.getPortIdentifiers();
        CommPortIdentifier portId;

        frameScale = CENTIGRADE;

        while (portList.hasMoreElements()) {
            portId = (CommPortIdentifier) portList.nextElement();
            if (portId.getPortType() == CommPortIdentifier.PORT_SERIAL) {
                logger.info("Trying to locate IndigoOmegaCamera at " + portId.getName());
                try {
                    serialPort = (SerialPort) portId.open("IndigoOmegaCamera", 1);
                    serialPort.setSerialPortParams(57600,
                                                   SerialPort.DATABITS_8,
                                                   SerialPort.STOPBITS_1,
                                                   SerialPort.PARITY_NONE);
                    serialPort.setFlowControlMode(SerialPort.FLOWCONTROL_NONE);
                    initialize();
                    return;
                } catch (Exception e) {
                    logger.debug("IndigoOmegaCamera error opening camera", e);
                    if (serialPort != null) {
                        serialPort.close();
                    }
                }
            }
        }

        throw(new IOException("Could not find camera."));
    }

    /**
     * Try and connect to the camera on the specified serialport. This will make
     * sure the is an Indigo Omega camera connected to the serialport and read
     * the status of the camera.
     *
     * @param serialPort the serialport to which the camera is conencted.
     * @throws IOException if no camera is conencted.
     */
    public IndigoOmegaCamera(SerialPort serialPort) throws IOException {
        this.serialPort = serialPort;
        initialize();
    }

    /**
     * Returns a string that is the camera name. This name can be used to
     * identify the camera, not necessarily the the exact camera, for instance
     * when two cameras are connected to the system.
     *
     * @return name of the camera
     */
    public String getName() {
        return "Indigo Omega";
    }

    public void showOptionsDialog() {
        options.show();
    }

    /**
     * Make sure a camera is conencted, and read the current camrea status.
     *
     * @throws IOException if no camera is connected.
     */
    private void initialize() throws IOException {
        this.input = serialPort.getInputStream();
        this.output = serialPort.getOutputStream();

        try {
            serialPort.enableReceiveTimeout(2000);
            if (!serialPort.isReceiveTimeoutEnabled())
                logger.warn("receiveTimeout not supported on serial port.");
        } catch (UnsupportedCommOperationException exc) {
            logger.warn("Could not set receive timeout.", exc);
        }
        serialPort.setInputBufferSize(1000);
        serialPort.disableReceiveThreshold();

        // send a no-op
        noOp();

        // get the status of the camera, if it works we have a valid camera
        getStatus();

        // get the serial number
        serialnumber = getSerialNumber();

        // output the camera info
        if (logger.isInfoEnabled()) {
            logger.info(this);
        }

        // initialize the options pane
        options = new IndigoOmegaCameraOptions(this);
    }

    /**
     * Close the serialport that controls the camera. Calling any function after
     * this will result in a null pointer excemption.
     */
    public void close() throws IOException {
        if (serialPort != null) {
            serialPort.close();
            serialPort = null;
            input = null;
            output = null;
        }
    }

    /**
     * Return a string with some camera information.
     *
     * @return camera information.
     */
    public String toString() {
        return getName() + " (S/N " + serialnumber + ")";
    }

    /**
     * No Operation.
     *
     * @throws IOException if command could not be executed correctly.
     */
    public void noOp() throws IOException {
        execute(NO_OP, null);
    }

    /**
     * Sets all current settings as power-on defaults.
     *
     * @throws IOException if command could not be executed correctly.
     */
    public void setDefaults() throws IOException {
        execute(SET_DEFAULTS, null);
    }

    /**
     * Commands a soft camera reset to the default modes.
     *
     * @throws IOException if command could not be executed correctly.
     */
    public void cameraReset() throws IOException {
        // resetting camera will not return a packet!
        byte[] packet = getPacket(CAMERA_RESET, null);
        writePacket(packet);

        // sleep for the camera
        try {
            Thread.sleep(500);
        } catch (InterruptedException exc) {}

        // get status
        getStatus();
    }

    /**
     * Restores factory settings as power-on defaults.
     *
     * @throws IOException if command could not be executed correctly.
     */
    public void restoreFactoryDefaults() throws IOException {
        execute(RESTORE_FACTORY_DEFAULTS, null);
        getStatus();
    }

    /**
     * Returns the serial number of the camera.
     *
     * @return the serial number of the camera.
     * @throws IOException if command could not be executed correctly.
     */
    public int getSerialNumber() throws IOException {
        byte[] data = new byte[4];
        execute(GET_SERIAL_NUMBER, data);
        return get32bit(data, 0);
    }

    /**
     * Returns the software revision.
     *
     * @return the revision number
     * @throws IOException if command could not be executed correctly.
     */
    public String getRevision() throws IOException {
        byte[] data = new byte[8];
        execute(GET_REVISION, data);
        return data[0] + " " + data[1] + " " + data[2] + " " + data[3] + " " +
               data[4] + " " + data[5] + " " + data[6] + " " + data[7];
    }

    /**
     * Returns the settings and modes of the camera.
     *
     * @return the status as string.
     * @throws IOException if command could not be executed correctly.
     */
    public String getStatus() throws IOException {
        byte[] data = new byte[4];
        execute(GET_STATUS, data);

        logger.info("Status : " + toString(data));

        // parse status
        spot = (byte) ((data[0] & 0x40) >> 6);
        isotherm = (byte) ((data[0] & 0x10) >> 4);
        testPattern = (byte) ((data[0] & 0x06) >> 1);
        lens = (byte) (((data[1] & 0x80) >> 7) + (data[0] & 0x01 << 1));
        //sync = (byte) ((data[2] & 0x06) >> 1);
        digitalOutputMode = (byte) ((data[2] & 0x30) >> 4);
        symbolColor = (byte) ((data[2] & 0xC0) >> 2);
        imageOrientation = (byte) (data[2] & 0x03);
        imageOptimization = (byte) ((data[3] & 0xC0) >> 6);
        polarity = (byte) ((data[3] & 0x20) >> 5);
        analogVideoMode = (byte) ((data[3] & 0x18) >> 3);
        fccMode = (byte) ((data[3] & 0x04) >> 2);
        dynamicRangeControlMode = (byte) ((data[3] & 0x03) >> 2);

        return toString(data);
    }

    /**
     * Returns the current dynamic range control mode.
     *
     * @return the currrent mode.
     */
    public byte getDynamicRangeControlMode() {
        return dynamicRangeControlMode;
    }

    /**
     * Sets the dynamic range control mode. This can be set to either
     * DYN_RNG_CTRL_AUTOMATIC, DYN_RNG_CTRL_HIGH_GAIN, DYN_RNG_CTRL_LOW_GAIN or
     * DYN_RNG_CTRL_DISABLED.
     *
     * @param mode the new dynamic range control mode.
     * @throws IOException if command could not be executed correctly.
     */
    public void setDynamicRangeControlMode(byte mode) throws IOException {

        byte[] data = new byte[]{0, mode};
        execute(DYN_RNG_CTRL_MODE, data);
        dynamicRangeControlMode = mode;
    }

    /**
     * Returns the FCC mode.
     *
     * @return the FCC mode.
     */
    public byte getFCCMode() {
        return fccMode;
    }

    /**
     * Sets the FCC mode. This can be set to either FCC_MANUAL or
     * FCC_AUTOMATIC.
     *
     * @param mode the new FCC mode.
     * @throws IOException if command could not be executed correctly.
     */
    public void setFCCMode(byte mode) throws IOException {
        byte[] data = new byte[]{0, mode};
        execute(FCC_MODE, data);
        fccMode = mode;
    }

    /**
     * Commands a flat field correction.
     *
     * @throws IOException if command could not be executed correctly.
     */
    public void doFCC() throws IOException {
        execute(DO_FCC, null);
    }

    /**
     * Returns the interval between automatic FCC in frames.
     *
     * @return frames between automatic FCC/
     * @throws IOException if command could not be executed correctly.
     */
    public int getFCCPeriod() throws IOException {
        byte[] data = new byte[2];
        execute(GET_FCC_PERIOD, data);
        return get16bit(data, 0);
    }

    /**
     * Sets the interval (in frames) between automatic FCC.
     *
     * @param period interval in frames.
     * @throws IOException if command could not be executed correctly.
     */
    public void setFCCPeriod(int period) throws IOException {
        byte[] data = new byte[2];
        get16bit(period, data, 0);
        execute(FCC_PERIOD, data);
    }

    /**
     * Returns the temperature differnce used to trigger automatic FCC.
     *
     * @return the temperature difference in centigrade.
     * @throws IOException if command could not be executed correctly.
     */
    public float getFCCTempDelta() throws IOException {
        byte[] data = new byte[2];
        execute(GET_FCC_TEMP_DELTA, data);
        return get16bit(data, 0) / 10.0f;
    }

    /**
     * Sets the temperature difference used to trigger automatic FCC in
     * centigrade.
     *
     * @param temp the temperature differnce.
     * @throws IOException if command could not be executed correctly.
     */
    public void setFCCTempDelta(float temp) throws IOException {
        byte[] data = new byte[2];
        get16bit((int) (temp * 10), data, 0);
        execute(FCC_TEMP_DELTA, data);
    }

    /**
     * Returns the current analog video signal mode. This can be either
     * ANALOG_VIDEO_FREEZE_FRAME or ANALOG_VIDEO_REAL_TIME.
     *
     * @return current analog video mode.
     */
    public byte getAnalogVideoMode() {
        return analogVideoMode;
    }

    /**
     * Sets the analog video signal mode. This can be set to either
     * ANALOG_VIDEO_FREEZE_FRAME or ANALOG_VIDEO_REAL_TIME.
     *
     * @param mode set analog video signal to this mode.
     * @throws IOException if command could not be executed correctly.
     */
    public void setAnalogVideoMode(byte mode) throws IOException {
        byte[] data = new byte[]{0x00, mode};
        execute(ANALOG_VIDEO_MODE, data);
        analogVideoMode = mode;
    }

    /**
     * Returns the current analog video polarity. This can be either
     * POLARITY_BLACK_HOT or POLARITY_WHITE_HOT.
     *
     * @return cirremt analog video polarity.
     */
    public byte getPolarity() {
        return polarity;
    }

    /**
     * Sets the analog video polarity. This can be set to either
     * POLARITY_BLACK_HOT or POLARITY_WHITE_HOT.
     *
     * @param mode sets polarity to this mode.
     * @throws IOException if command could not be executed correctly.
     */
    public void setPolarity(byte mode) throws IOException {
        byte[] data = new byte[]{0, mode};
        execute(POLARITY, data);
        polarity = mode;
    }

    /**
     * Returns the current analog video orientation. This can either be
     * IMAGE_ORIENTATION_NORMAL or IMAGE_ORIENTATION_REVERT.
     *
     * @return the current analog video orientation.
     */
    public byte getImageOrientation() {
        return imageOrientation;
    }

    /**
     * Sets the analog video orientation. This can be set to either
     * IMAGE_ORIENTATION_NORMAL or IMAGE_ORIENTATION_REVERT.
     *
     * @param mode set the imageorientation to this mode.
     * @throws IOException if command could not be executed correctly.
     */
    public void setImageOrientation(byte mode) throws IOException {
        byte[] data = new byte[]{0, mode};
        execute(IMAGE_ORIENTATION, data);
        imageOrientation = mode;
    }

    /**
     * Returns teh current digital output mode. This can be either
     * DIGITAL_OUTPUT_14_BIT or DIGITAL_OUTPUT_8_BIT.
     *
     * @return current digital output mode.
     */
    public byte getDigitalOutputMode() {
        return digitalOutputMode;
    }

    /**
     * Sets the digital output channel mode. This can be set to either
     * DIGITAL_OUTPUT_14_BIT or DIGITAL_OUTPUT_8_BIT.
     *
     * @param mode set the digital output to this mode.
     * @throws IOException if command could not be executed correctly.
     */
    public void setDigitalOutputMode(byte mode) throws IOException {
        byte[] data = new byte[]{0, mode};
        execute(DIGITAL_OUTPUT_MODE, data);
        digitalOutputMode = mode;
    }

    /**
     * Returns the current image optimization mode. This can be either
     * IMAGE_OPTIMIZATION_AUTOMATIC, IMAGE_OPTIMIZATION_MANUAL_OPTIMIZED,
     * IMAGE_OPTIMIZATION_AUTO_BRIGHTNESS or IMAGE_OPTIMIZATION_MANUAL_FIXED.
     *
     * @return current image optimization mode.
     */
    public byte getImageOptimization() {
        return imageOptimization;
    }

    /**
     * Sets the image optimization mode. This can be set to either
     * IMAGE_OPTIMIZATION_AUTOMATIC, IMAGE_OPTIMIZATION_MANUAL_OPTIMIZED,
     * IMAGE_OPTIMIZATION_AUTO_BRIGHTNESS or IMAGE_OPTIMIZATION_MANUAL_FIXED.
     *
     * @param mode set the image optimization mode to this mode.
     * @throws IOException if command could not be executed correctly.
     */
    public void setImageOptimization(byte mode) throws IOException {
        byte[] data = new byte[]{0, mode};
        execute(IMAGE_OPTIMIZATION_MODE, data);
        imageOptimization = mode;
    }

    /**
     * Returns the current contrast value.
     *
     * @return constrast value
     * @throws IOException if command could not be executed correctly.
     */
    public int getContrast() throws IOException {
        byte[] data = new byte[2];
        execute(GET_CONSTRAST, data);
        return get16bit(data, 0);
    }

    /**
     * Sets the manual LUT contrast value.
     *
     * @param val the new contrast value.
     * @throws IOException if command could not be executed correctly.
     */
    public void setContrast(int val) throws IOException {
        byte[] data = new byte[2];
        get16bit(val, data, 0);
        execute(CONTRAST, data);
    }

    /**
     * Returns the current brightness value.
     *
     * @return brightness value
     * @throws IOException if command could not be executed correctly.
     */
    public int getBrightness() throws IOException {
        byte[] data = new byte[2];
        execute(GET_BRIGHTNESS, data);
        return get16bit(data, 0);
    }

    /**
     * Sets the manual LUT brightness value.
     *
     * @param val the new brightness value.
     * @throws IOException if command could not be executed correctly.
     */
    public void setBrightness(int val) throws IOException {
        byte[] data = new byte[2];
        get16bit(val, data, 0);
        execute(BRIGHTNESS, data);
    }

    /**
     * Returns the auto-brightness LUT bias value.
     *
     * @return auto-brightness value
     * @throws IOException if command could not be executed correctly.
     */
    public int getBrightnessBias() throws IOException {
        byte[] data = new byte[2];
        execute(GET_BRIGHTNESS_BIAS, data);
        return get16bit(data, 0);
    }

    /**
     * Sets the auto-brightness LUT bias value.
     *
     * @param val the new brightness bias value.
     * @throws IOException if command could not be executed correctly.
     */
    public void setBrightnessBias(int val) throws IOException {
        byte[] data = new byte[2];
        get16bit(val, data, 0);
        execute(BRIGHTNESS_BIAS, data);
    }

    /**
     * Returns the current color for the on-screen symbols. This can either be
     * SYMBOL_COLOR_BLACK_WHITE, SYMBOL_COLOR_OVERBRIGHT or SYMBOL_COLOR_OFF.
     *
     * @return current color.
     */
    public byte getSymbolColor() {
        return symbolColor;
    }

    /**
     * Sets the color of on-screen symbols. This can be set to either
     * SYMBOL_COLOR_BLACK_WHITE, SYMBOL_COLOR_OVERBRIGHT or SYMBOL_COLOR_OFF.
     *
     * @param mode sets symbolcolor to this mode.
     * @throws IOException if command could not be executed correctly.
     */
    public void setSymbolColor(byte mode) throws IOException {
        byte[] data = new byte[]{0, mode};
        execute(SYMBOL_COLOR, data);
        symbolColor = mode;
    }

    /**
     * Returns the current lens number.
     *
     * @return lens number.
     */
    public byte getLens() {
        return lens;
    }

    /**
     * Specifies the calibrated lens number.
     *
     * @param mode sets the lens number.
     * @throws IOException if command could not be executed correctly.
     */
    public void setLens(byte mode) throws IOException {
        byte[] data = new byte[]{0, mode};
        execute(SET_LENS, data);
        lens = mode;
    }

    /**
     * Returns the spot meter mode. This can be either SPOT_METER_DISABLED,
     * SPOT_METER_ON_FAHRENHEIT or SPOT_METER_ON_CENTIGRADE.
     *
     * @return spot meter mode.
     */
    public byte getSpotMeter() {
        return spot;
    }

    /**
     * Enables or disables the spot-meter. Not all cameras have this optional
     * capability. The fahrenheit scale only applies to on-screen disply. The
     * getSpotMeter() command alsways returns the value in Celcius.
     *
     * @param mode sets the lens number.
     * @throws IOException if command could not be executed correctly.
     */
    public void setSpotMeter(byte mode) throws IOException {
        byte[] data = new byte[]{0, mode};
        execute(SPOT_METER_MODE, data);
        spot = mode;
    }

    /**
     * Returns the value (in Celsius degrees) of the spot-meter.
     *
     * @return the value of the spot meter.
     * @throws IOException if command could not be executed correctly.
     */
    public int getSpotMeterValue() throws IOException {
        byte[] data = new byte[2];
        execute(GET_SPOT_METER, data);
        return get16bit(data, 0);
    }

    /**
     * Gets the case temperature. The temperature will be converted to the right
     * temperature scale. The scale can be either KELVIN, CENTIGRADE or
     * FAHRENHEIT.
     *
     * @param scale the temperature scale to which to convert the case
     *              temperature.
     * @return the temperature of the case.
     * @throws IOException if command could not be executed correctly.
     */
    public float getCaseTemp(int scale) throws IOException {
        byte[] data = new byte[2];
        execute(GET_CASE_TEMP, data);
        float kelvin = get16bit(data, 0) / 10.0f;

        switch (scale) {
            case KELVIN:
                return kelvin;

            case CENTIGRADE:
                return kelvin - 273.15f;

            case FAHRENHEIT:
                return ((kelvin - 273.15f) * 1.8f) + 32.0f;

            default:
                return kelvin;
        }
    }

    /**
     * Returns current isotherm mode. This can be either ISOTHERM_ENABLED or
     * ISOTHERM_DISABLED.
     *
     * @return isotherm
     */
    public byte getIsotherm() {
        return isotherm;
    }

    /**
     * Enables or disables isotherm. Not all cameras have this optional
     * capability. This can be set to either ISOTHERM_ENABLED or
     * ISOTHERM_DISABLED.
     *
     * @param mode sets the isotherm to this mode.
     * @throws IOException if command could not be executed correctly.
     */
    public void setIsotherm(byte mode) throws IOException {
        byte[] data = new byte[]{0, mode};
        execute(ISOTHERM_MODE, data);
        isotherm = mode;
    }

    /**
     * Gets the isotherm upper and lower threshold.
     *
     * @return upper (return[0]) and lower (return[1]) threshold.
     * @throws IOException if command could not be executed correctly.
     */
    public int[] getIsothermThreshold() throws IOException {
        int[] result = new int[2];
        byte[] data = new byte[4];
        execute(GET_ISOTHERM_LIMITS, data);
        result[0] = get16bit(data, 0);
        result[1] = get16bit(data, 2);
        return result;
    }

    /**
     * Sets the isotherm red and yellow thresholds. Each threshold is expressed
     * as a percentage of the maximum scene range (in Centigrade). (e.g.
     * threshold value of 90 decimal represents 90% = 135C in high-gain, = 450C
     * in low gain).
     *
     * @param red    the red threshold
     * @param yellow the yellow threshold
     * @throws IOException if command could not be executed correctly.
     */
    public void setIsothermThreshold(int red, int yellow) throws IOException {
        byte[] data = new byte[4];
        get16bit(yellow, data, 0);
        get16bit(red, data, 2);
        execute(SET_ISOTHERM_THRESHOLDS, data);
    }

    /**
     * This will return the scale in which getFrame() will return the image.
     * This can be either KELVIN, CENTIGRADE, FAHRENHEIT or RAW.
     *
     * @return scale for getFrame()
     */
    public int getFrameScale() {
        return frameScale;
    }

    /**
     * This will set the scale in which getFrame() will return the image. This
     * can be set to either KELVIN, CENTIGRADE, FAHRENHEIT or RAW.
     */
    public void setFrameScale(int scale) {
        frameScale = scale;
    }

    /**
     * This will return the current test pattern. This can be either
     * TEST_PATTERN_OFF, TEST_PATTERN_RAMP or TEST_PATTERN_SHADE.
     *
     * @return test pattern.
     */
    public byte getTestPattern() {
        return testPattern;
    }

    /**
     * Sets test pattern modes. This can be set to either  TEST_PATTERN_OFF,
     * TEST_PATTERN_RAMP or TEST_PATTERN_SHADE.
     *
     * @param mode the test pattern to be shown.
     * @throws IOException if command could not be executed correctly.
     */
    public void setTestPattern(byte mode) throws IOException {
        byte[] data = new byte[]{0, mode};
        execute(TEST_PATTERN_MODE, data);
        testPattern = mode;
    }

    /**
     * Return a list correlating lens number to calibrated lens type. ID
     * 0x00&nbsp;=&nbsp;not calibrated, 0x01&nbsp;=&nbsp;8.5mm,
     * 0x02&nbsp;=&nbsp;11mm, 0x03&nbsp;=&nbsp;18m, 0x04&nbsp;=&nbsp;30mm.
     *
     * @return each lens their ID. Each byte is a lens.
     * @throws IOException if command could not be executed correctly.
     */
    public byte[] getLensID() throws IOException {
        byte[] data = new byte[4];
        execute(GET_LENS_ID, data);
        return data;
    }

    /**
     * Returns an encoded list of enabled options. The list is a bit mask for
     * each option installed. The options are OPTION_AUTO_DYN_RNG,
     * OPTION_DIGITAL_OUTPUT, OPTION_EXTENDED_TEMP, OPTION_ISOTHERM or
     * OPTION_SPOT_METER.
     *
     * @return the options of the camera.
     * @throws IOException if command could not be executed correctly.
     */
    public int getCameraOptions() throws IOException {
        byte[] data = new byte[2];
        execute(GET_CAMERA_OPTIONS, data);
        return get16bit(data, 0);
    }

    /**
     * Checks to see if the camera has all options request. The mask is a bit
     * mask with options requested. The options are OPTION_AUTO_DYN_RNG,
     * OPTION_DIGITAL_OUTPUT, OPTION_EXTENDED_TEMP, OPTION_ISOTHERM or
     * OPTION_SPOT_METER.
     *
     * @param mask the list of options to check.
     * @return true if all options are present, false otherwise.
     * @throws IOException if command could not be executed correctly.
     */
    public boolean hasCameraOption(int mask) throws IOException {
        return (getCameraOptions() & mask) == mask;
    }

    /**
     * Returns the current frame captured from the camera. Depending on the
     * current digital output mode this will either be a unsigned short image
     * (DIGITAL_OUTPUT_14_BIT) or a byte image (DIGITAL_OUTPUT_8_BIT). This will
     * execute a doFCC() first.
     *
     * @return the current image captured from the camera.
     * @throws IOException if command could not be executed correctly.
     */
    public ImageObject getFrame() throws IOException {
        doFCC();
        // wait a little bit after FCC to capture frame
        try {
            Thread.sleep(500);
        } catch (InterruptedException exc) {}
        return getFrame(getDigitalOutputMode(), frameScale);
    }

    /**
     * Returns the current frame captured from the camera. Depending on the mode
     * specified this will either be a unsigned short image
     * (DIGITAL_OUTPUT_14_BIT) or a byte image (DIGITAL_OUTPUT_8_BIT). For best
     * results it is suggested to call doFCC() before calling this function.
     *
     * @param mode  the mode in which to take the image.
     * @param scale the values in the returned image are converted to this.
     * @return the current image captured from the camera.
     * @throws IOException if command could not be executed correctly.
     */
    public ImageObject getFrame(byte mode, int scale) throws IOException {
        int w, h, dlen, i, r, c, idx;
        ImageObject result;

        // stop video grabbing
        byte amode = analogVideoMode;
        setAnalogVideoMode(ANALOG_VIDEO_FREEZE_FRAME);

        // read the image, this depends on the mode we are in
        if (mode == DIGITAL_OUTPUT_8_BIT) {
            w = 160; // should be 160
            h = 120; // NTSC = 120, PAL = 128
            dlen = 164;

            // allocate some variables
            byte[] data = new byte[]{0, mode, 0, 0};
            byte[] packet = new byte[dlen + 10];
            byte[] image = new byte[w * h];

            for (idx = 0, r = 0; r < h; r++) {
                // send request for a row of data
                data[3] = (byte) r;
                writePacket(getPacket(DOWNLOAD_ROW, data));

                // read packet and check it
                readPacket(packet);
                checkPacket(DOWNLOAD_ROW, dlen, packet);

                // copy data
                System.arraycopy(packet, 12, image, idx, w);
                idx += w;
                fireProgress(r, h - 1);
            }

            result = new ImageObjectByte(h, w, 1);
            result.setData(image);

        } else {
            w = 162;
            h = 128;
            dlen = 328;

            // allocate some variables
            short s;
            byte[] data = new byte[]{0, mode, 0, 0};
            byte[] packet = new byte[dlen + 10];
            short[] image_s = null;
            double[] image_d = null;
            if ((scale == KELVIN) || (scale == CENTIGRADE) || (scale == FAHRENHEIT)) {
                image_d = new double[w * h];
            } else {
                image_s = new short[w * h];
            }

            for (idx = 0, r = 0; r < h; r++) {
                // send request for a row of data
                data[3] = (byte) r;
                writePacket(getPacket(DOWNLOAD_ROW, data));

                // read packet and check it
                readPacket(packet);
                checkPacket(DOWNLOAD_ROW, dlen, packet);

                // copy data
                for (i = 12, c = 0; c < w; c++, idx++, i += 2) {
                    s = (short) (((packet[i] & 0xff) << 8) + (packet[i + 1] & 0xff));
                    if ((scale == KELVIN) || (scale == CENTIGRADE) || (scale == FAHRENHEIT)) {
                        image_d[idx] = s * conv_m[scale] + conv_b[scale];
                    } else {
                        image_s[idx] = s;
                    }

                }
                fireProgress(r, h - 1);
            }

            if (image_s == null) {
                result = new ImageObjectDouble(h, w, 1);
                result.setData(image_d);
            } else {
                result = new ImageObjectUShort(h, w, 1);
                result.setData(image_s);
            }
        }

        // back to original state
        setAnalogVideoMode(amode);

        // create the image
        return result;
    }

    /**
     * Execute the command. This will send the command to the camera and wait
     * for a result. After receiving the result it will do some sanity checks on
     * the result.
     *
     * @param function the function to execute.
     * @param data     any additional data needed for the function.
     * @throws IOException if command could not be executed correctly.
     */
    private void execute(byte function, byte[] data) throws IOException {
        // write the function
        byte[] packet = getPacket(function, data);
        writePacket(packet);

        // read the result
        readPacket(packet);

        // check packet
        int datalen = (data == null) ? 0 : data.length;
        checkPacket(function, datalen, packet);

        // copy the resulting data back
        if (data != null) {
            System.arraycopy(packet, 8, data, 0, datalen);
        }
    }

    /**
     * Writes a packet to the camera.
     *
     * @param packet the packet to send to the camera.
     * @throws IOException if packet could not be send.
     */
    private void writePacket(byte[] packet) throws IOException {
        //logger.debug("> " + toString(packet));
        output.write(packet);
    }

    /**
     * Reads a packet from the camera. This will get a complete packet from the
     * camera.
     *
     * @param packet array in which packet will be read.
     * @return number of bytes in packet.
     * @throws IOException if packet could not be read correctly.
     */
    private int readPacket(byte[] packet) throws IOException {
        // wait for result
        int off = 0;
        int read = 0;
        int len = 10;

        // read the first 10 bytes, that is full message if no data
        // this count does include the 2 CRC bytes at the end
        while (len > 0) {
            read = input.read(packet, off, len);
            if (read < 0) {
                throw(new IOException("Unexpected end of stream."));
            }
            if (read == 0) {
                throw(new IOException("No resulting data."));
            }
            off += read;
            len -= read;
        }

        // read the data
        len = get16bit(packet, 4);
        if (packet.length < (len + 10)) {
            throw(new IOException("Allocated space is not correct."));
        }
        while (len > 0) {
            read = input.read(packet, off, len);
            if (read < 0) {
                throw(new IOException("Unexpected end of stream."));
            }
            if (read == 0) {
                throw(new IOException("No resulting data."));
            }
            off += read;
            len -= read;
        }

        //logger.debug("< " + toString(packet));
        return off;
    }

    /**
     * Check the packet for correctness. This will make sure that the packet has
     * a OK status byte, right length and correct CRCs.
     *
     * @param function expected function.
     * @param datalen  expected datalength
     * @param packet   the packet to be checked.
     * @throws IOException if packet is not a valid packet.
     */
    private void checkPacket(byte function, int datalen, byte[] packet) throws IOException {
        // make sure return packet starts with 0x6e
        if (packet[0] != MAGIC_BYTE) {
            throw(new IOException("Invalid packet."));
        }

        // check status
        if (packet[1] != UL3_OK) {
            throw(new IOException(getStatus(packet[1])));
        }

        // check function code
        if (packet[3] != function) {
            throw(new IOException("Got different function results."));
        }

        // check datalen
        int len = get16bit(packet, 4);
        if (len != datalen) {
            //throw(new IOException("Incorrect number of bytes returned."));
        }

        // check header crc
        int crc = get16bit(packet, 6);
        if (calcCRC16Bytes(packet, 6) != crc) {
            throw(new IOException("Invalid header CRC."));
        }

        // check packet crc
        int packlen = packet.length;
        crc = get16bit(packet, packlen - 2);
        if (calcCRC16Bytes(packet, packlen - 2) != crc) {
            throw(new IOException("Invalid packet CRC."));
        }
    }

    /**
     * Construct a camera packet. This will create a packet for the function and
     * the data. After allocating the right amount of space the code will create
     * a packet, insert the data and calculate the CRC checksums. The returned
     * packet is ready to be send to the camera.
     *
     * @param function the function number to send to the camera.
     * @param data     the data that goes with the function, or null if no data
     *                 is needed with the function.
     * @return the packet itself.
     */
    private byte[] getPacket(byte function, byte[] data) {
        int datalen = (data == null) ? 0 : data.length;
        int packlen = datalen + 10;
        byte[] packet = new byte[packlen];

        // all packets this is set to 0x6e both incoming and outgoing.
        packet[0] = MAGIC_BYTE;

        // status only interesting incoming.
        packet[1] = 0x00;

        // reserved
        packet[2] = 0x00;

        // function
        packet[3] = function;

        // byte count
        get16bit(datalen, packet, 4);

        // crc of packet sofar
        int crc = calcCRC16Bytes(packet, 6);
        get16bit(crc, packet, 6);

        // copy the data
        if (datalen != 0) {
            System.arraycopy(data, 0, packet, 8, datalen);
        }

        // calculate the crc of packet incl data and prev crc
        crc = calcCRC16Bytes(packet, packlen - 2);
        get16bit(crc, packet, packlen - 2);

        // return the packet
        return packet;
    }

    /**
     * Translate the status byte to a string representation.
     *
     * @param status the status byte that needs translating.
     * @return an informative string about the status.
     */
    private String getStatus(byte status) {
        switch (status) {
            case UL3_OK:
                return "Function executed.";

            case UL3_BUSY:
                return "Camera busy processing command.";

            case UL3_NOT_READY:
                return "Camera not ready to execute specified command.";

            case UL3_RANGE_ERROR:
                return "Data out of range.";

            case UL3_CHECKSUM_ERROR:
                return "Header or message-body checksum error.";

            case UL3_UNDEFINED_PROCESS_ERROR:
                return "Unknown process code.";

            case UL3_UNDEFINED_FUNCTION_ERROR:
                return "Unknown function code.";

            case UL3_TIMEOUT_ERROR:
                return "Timeout executing command.";

            case UL3_BYTE_COUNT_ERROR:
                return "Byte count incorrect for the function code.";

            case UL3_FEATURE_NOT_ENABLED:
                return "Function code not enabled in the current camera configuration.";

            default:
                return "Unknown status.";
        }
    }

    /**
     * helper function to translate bytes to short.
     */
    private int get16bit(byte[] data, int off) {
        return ((data[off] & 0xff) << 8) + (data[off + 1] & 0xff);
    }

    /**
     * helper function to translate short to bytes.
     */
    private void get16bit(int val, byte[] data, int off) {
        data[off + 0] = (byte) ((val & 0xff00) >> 8);
        data[off + 1] = (byte) (val & 0x00ff);
    }

    /**
     * helper function to translate bytes to int.
     */
    private int get32bit(byte[] data, int off) {
        return ((data[off + 0] & 0xff) << 24) + ((data[off + 1] & 0xff) << 16) +
               ((data[off + 2] & 0xff) << 8) + (data[off + 3] & 0xff);
    }

    /**
     * helper function to translate int to bytes.
     */
    private void get32bit(int val, byte[] data, int off) {
        data[off + 0] = (byte) ((val & 0xff000000) >> 24);
        data[off + 1] = (byte) ((val & 0x00ff0000) >> 16);
        data[off + 2] = (byte) ((val & 0x0000ff00) >> 8);
        data[off + 3] = (byte) (val & 0x000000ff);
    }

    /**
     * helper function to translate bytes to long.
     */
    private long get64bit(byte[] data, int off) {
        return ((data[off + 0] & 0xff) << 56) + ((data[off + 1] & 0xff) << 48) +
               ((data[off + 2] & 0xff) << 40) + ((data[off + 3] & 0xff) << 32) +
               ((data[off + 4] & 0xff) << 24) + ((data[off + 5] & 0xff) << 16) +
               ((data[off + 6] & 0xff) << 8) + (data[off + 7] & 0xff);
    }

    /**
     * Convert a set of bytes to a hex representation.
     *
     * @param data the data to be displayed.
     * @return a string with hex representation of the data.
     */
    private String toString(byte[] data) {
        BigInteger big = new BigInteger(data);
        String result = big.toString(16);
        if (result.length() % 2 != 0) {
            result = "0" + result;
        }
        return result;
    }

/*******************************************************************************
 * University of Illinois/NCSA
 * Open Source License according to
 * http://www.otm.uiuc.edu/faculty/forms/opensource.asp
 * 
 * Copyright (c) 2006,    NCSA/UIUC.  All rights reserved.
 * 
 * Developed by:
 * 
 * Name of Development Groups:
 * Image Spatial Data Analysis Group (ISDA Group)
 * http://isda.ncsa.uiuc.edu/
 * 
 * Name of Institutions:
 * National Center for Supercomputing Applications (NCSA)
 * http://www.ncsa.uiuc.edu/
 * 
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the 
 * "Software"), to deal with the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 * 
 * - Redistributions of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimers.
 * - Redistributions in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimers in the
 *   documentation and/or other materials provided with the distribution.
 *   Neither the names of University of Illinois/NCSA, nor the names
 *   of its contributors may be used to endorse or promote products
 *   derived from this Software without specific prior written permission.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE CONTRIBUTORS OR COPYRIGHT HOLDERS BE LIABLE FOR
 * ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF 
 * CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS WITH THE SOFTWARE.
 *******************************************************************************/

    //    /*
    //     *  ===== ByteCRC16 =====
    //     *      Calculate (update) the CRC16 for a single 8-bit byte
    //     */
    //    int ByteCRC16(int value, int crcin)
    //    {
    //        int k = (((crcin >> 8) ^ value) & 255) << 8;
    //        int crc = 0;
    //        int bits = 8;
    //        do
    //        {
    //            if (( crc ^ k ) & 0x8000)
    //                crc = (crc << 1) ^ 0x1021;
    //            else
    //                crc <<= 1;
    //            k <<= 1;
    //        }
    //        while (--bits);
    //        return ((crcin << 8) ^ crc);
    //    }

    private int byteCRC16(int v, int crc) {
        return ((crc << 8) ^ ccitt_16Table[((crc >> 8) ^ (v)) & 255]);
    }

    /*
     *  ===== CalcCRC16Words =====
     *      Calculate the CRC for a buffer of 16-bit words.  Supports both
     *  Little and Big Endian formats using conditional compilation.
     *      Note: minimum count is 1 (0 case not handled)
     */
    private int calcCRC16Words(short[] buffer, int len) {
        int value;
        int crc = 0;
        for (int i = 0; i < len; i++) {
            value = (buffer[i] & 0xffff);
            //    #ifdef _BIG_ENDIAN
            crc = byteCRC16(value >> 8, crc);
            crc = byteCRC16(value, crc);
            //    #else
            //            crc = ByteCRC16(value, crc);
            //            crc = ByteCRC16(value >> 8, crc);
            //    #endif
        }
        return (crc & 0xffff);
    }

    /*
     *  ===== CalcCRC16Bytes =====
     *      Calculate the CRC for a buffer of 8-bit words.
     *      Note: minimum count is 1 (0 case not handled)
     */
    private int calcCRC16Bytes(byte[] buffer, int len) {
        int value;
        int crc = 0;
        for (int i = 0; i < len; i++) {
            value = (buffer[i] & 0xff);
            crc = byteCRC16(value, crc);
        }
        return (crc & 0xffff);
    }

    static final int ccitt_16Table[] = {
        0x0000, 0x1021, 0x2042, 0x3063, 0x4084, 0x50A5, 0x60C6, 0x70E7,
        0x8108, 0x9129, 0xA14A, 0xB16B, 0xC18C, 0xD1AD, 0xE1CE, 0xF1EF,
        0x1231, 0x0210, 0x3273, 0x2252, 0x52B5, 0x4294, 0x72F7, 0x62D6,
        0x9339, 0x8318, 0xB37B, 0xA35A, 0xD3BD, 0xC39C, 0xF3FF, 0xE3DE,
        0x2462, 0x3443, 0x0420, 0x1401, 0x64E6, 0x74C7, 0x44A4, 0x5485,
        0xA56A, 0xB54B, 0x8528, 0x9509, 0xE5EE, 0xF5CF, 0xC5AC, 0xD58D,
        0x3653, 0x2672, 0x1611, 0x0630, 0x76D7, 0x66F6, 0x5695, 0x46B4,
        0xB75B, 0xA77A, 0x9719, 0x8738, 0xF7DF, 0xE7FE, 0xD79D, 0xC7BC,
        0x48C4, 0x58E5, 0x6886, 0x78A7, 0x0840, 0x1861, 0x2802, 0x3823,
        0xC9CC, 0xD9ED, 0xE98E, 0xF9AF, 0x8948, 0x9969, 0xA90A, 0xB92B,
        0x5AF5, 0x4AD4, 0x7AB7, 0x6A96, 0x1A71, 0x0A50, 0x3A33, 0x2A12,
        0xDBFD, 0xCBDC, 0xFBBF, 0xEB9E, 0x9B79, 0x8B58, 0xBB3B, 0xAB1A,
        0x6CA6, 0x7C87, 0x4CE4, 0x5CC5, 0x2C22, 0x3C03, 0x0C60, 0x1C41,
        0xEDAE, 0xFD8F, 0xCDEC, 0xDDCD, 0xAD2A, 0xBD0B, 0x8D68, 0x9D49,
        0x7E97, 0x6EB6, 0x5ED5, 0x4EF4, 0x3E13, 0x2E32, 0x1E51, 0x0E70,
        0xFF9F, 0xEFBE, 0xDFDD, 0xCFFC, 0xBF1B, 0xAF3A, 0x9F59, 0x8F78,
        0x9188, 0x81A9, 0xB1CA, 0xA1EB, 0xD10C, 0xC12D, 0xF14E, 0xE16F,
        0x1080, 0x00A1, 0x30C2, 0x20E3, 0x5004, 0x4025, 0x7046, 0x6067,
        0x83B9, 0x9398, 0xA3FB, 0xB3DA, 0xC33D, 0xD31C, 0xE37F, 0xF35E,
        0x02B1, 0x1290, 0x22F3, 0x32D2, 0x4235, 0x5214, 0x6277, 0x7256,
        0xB5EA, 0xA5CB, 0x95A8, 0x8589, 0xF56E, 0xE54F, 0xD52C, 0xC50D,
        0x34E2, 0x24C3, 0x14A0, 0x0481, 0x7466, 0x6447, 0x5424, 0x4405,
        0xA7DB, 0xB7FA, 0x8799, 0x97B8, 0xE75F, 0xF77E, 0xC71D, 0xD73C,
        0x26D3, 0x36F2, 0x0691, 0x16B0, 0x6657, 0x7676, 0x4615, 0x5634,
        0xD94C, 0xC96D, 0xF90E, 0xE92F, 0x99C8, 0x89E9, 0xB98A, 0xA9AB,
        0x5844, 0x4865, 0x7806, 0x6827, 0x18C0, 0x08E1, 0x3882, 0x28A3,
        0xCB7D, 0xDB5C, 0xEB3F, 0xFB1E, 0x8BF9, 0x9BD8, 0xABBB, 0xBB9A,
        0x4A75, 0x5A54, 0x6A37, 0x7A16, 0x0AF1, 0x1AD0, 0x2AB3, 0x3A92,
        0xFD2E, 0xED0F, 0xDD6C, 0xCD4D, 0xBDAA, 0xAD8B, 0x9DE8, 0x8DC9,
        0x7C26, 0x6C07, 0x5C64, 0x4C45, 0x3CA2, 0x2C83, 0x1CE0, 0x0CC1,
        0xEF1F, 0xFF3E, 0xCF5D, 0xDF7C, 0xAF9B, 0xBFBA, 0x8FD9, 0x9FF8,
        0x6E17, 0x7E36, 0x4E55, 0x5E74, 0x2E93, 0x3EB2, 0x0ED1, 0x1EF0
    };
}
