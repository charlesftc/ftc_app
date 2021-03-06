package org.firstinspires.ftc.teamcode;

import com.qualcomm.hardware.bosch.BNO055IMU;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.Gamepad;
import com.qualcomm.robotcore.util.Range;

import org.firstinspires.ftc.robotcore.external.Func;
import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.robotcore.external.navigation.AxesOrder;
import org.firstinspires.ftc.robotcore.external.navigation.AxesReference;
import org.firstinspires.ftc.robotcore.external.navigation.Orientation;

import java.util.Locale;

public class AutoDrive {
    //private ElapsedTime runtime = new ElapsedTime();
    private LinearOpMode opmode;
    private Gamepad gamepad;

    private DcMotor leftDrive;
    private DcMotor rightDrive;

    private int startTicksLeft;
    private int startTicksRight;

    /*private double drivePower = 0.3;
    private double turnPower = 0.25;

    private double slowDrivePower = 0.1;
    private double slowTurnPower = 0.05;*/

    private double turnPower = 0.25;

    private double driveAdjustment = -2;

    private int ticksPerInch = 89;
    private double kDrive = 0.0015;
    private double kTurn = 0.01;

    BNO055IMU imu;

    Telemetry.Item myStuff;

    public AutoDrive(LinearOpMode opmode, Gamepad gamepad) {
        this.opmode = opmode;
        this.gamepad = gamepad;

        leftDrive  = opmode.hardwareMap.get(DcMotor.class, "left_drive");
        rightDrive = opmode.hardwareMap.get(DcMotor.class, "right_drive");

        leftDrive.setDirection(DcMotor.Direction.FORWARD);
        rightDrive.setDirection(DcMotor.Direction.REVERSE);
        leftDrive.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        rightDrive.setMode(DcMotor.RunMode.RUN_USING_ENCODER);

        initImu();

        myStuff = opmode.telemetry.addData("AutoDrive", "");

        resetDistance();

        opmode.telemetry.addLine().addData("Stuff", new Func<String>() {
            @Override
            public String value() {
                return String.format(Locale.getDefault(), "currentHeading %.2f, inches %.2f",
                        getHeading(), distanceTraveled());
            }
        });
    }

    private void initImu() {
        BNO055IMU.Parameters parameters = new BNO055IMU.Parameters();
        parameters.angleUnit = BNO055IMU.AngleUnit.DEGREES;
        parameters.accelUnit = BNO055IMU.AccelUnit.METERS_PERSEC_PERSEC;
        parameters.loggingEnabled = true;
        parameters.loggingTag = "imu";
        //parameters.accelerationIntegrationAlgorithm = new JustLoggingAccelerationIntegrator();
        imu = opmode.hardwareMap.get(BNO055IMU.class, "imu");
        imu.initialize(parameters);

        while (!opmode.isStopRequested() && !imu.isGyroCalibrated()) {
            opmode.sleep(50);
            opmode.idle();
        }
    }

    public void drive(double inches, double drivePower) {
        resetDistance();
        double heading = getHeading();

        double distance = inches;

        if (Math.abs(inches) > Math.abs(driveAdjustment)) {
            distance += (driveAdjustment * Math.copySign(1.0, inches));
        }

        while (opmode.opModeIsActive() && Math.abs(distanceTraveled()) < Math.abs(distance)) {
            double errorHeading = getHeading() - heading;
            double turn = errorHeading * kDrive;
            turn = Range.clip(turn, -turnPower, turnPower);

            double sign = Math.copySign(1.0, inches);
            leftDrive.setPower((sign * drivePower) + turn);
            rightDrive.setPower((sign * drivePower) - turn);

            myStuff.setValue("errorHeading %.2f, heading %.2f, turn %.2f", errorHeading, heading, turn);
            opmode.telemetry.update();

            opmode.sleep(20);
        }

        leftDrive.setPower(0);
        rightDrive.setPower(0);
    }

    public void powerDrive(double inches, double drivePower) {
        leftDrive.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        rightDrive.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);

        resetDistance();
        double heading = getHeading();

        double distance = inches;

        if (Math.abs(inches) > Math.abs(driveAdjustment)) {
            distance += (driveAdjustment * Math.copySign(1.0, inches));
        }

        while (opmode.opModeIsActive() && Math.abs(distanceTraveled()) < Math.abs(distance)) {
            double errorHeading = getHeading() - heading;
            double turn = errorHeading * kDrive;
            turn = Range.clip(turn, -turnPower, turnPower);

            double sign = Math.copySign(1.0, inches);
            leftDrive.setPower((sign * drivePower) + turn);
            rightDrive.setPower((sign * drivePower) - turn);

            myStuff.setValue("errorHeading %.2f, heading %.2f, turn %.2f", errorHeading, heading, turn);
            opmode.telemetry.update();

            opmode.sleep(20);
        }

        leftDrive.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        leftDrive.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        leftDrive.setPower(0);
        rightDrive.setPower(0);
    }

    public void turn(double heading) {
        double errorHeading;

        do {
            errorHeading = getHeading() - heading;
            double turn = errorHeading * kTurn;
            turn = Range.clip(turn, -turnPower, turnPower);

            leftDrive.setPower(turn);
            rightDrive.setPower(-turn);

            /*myStuff.setValue("errorHeading %.2f, heading %.2f, turn %.2f", errorHeading, heading, turn);
            opmode.telemetry.update();*/

            opmode.sleep(20);
        } while (opmode.opModeIsActive() && Math.abs(errorHeading) > 1.5);

        leftDrive.setPower(0);
        rightDrive.setPower(0);
    }

    public double getHeading(){
        Orientation angles = imu.getAngularOrientation(AxesReference.INTRINSIC, AxesOrder.ZYX, AngleUnit.DEGREES);
        return angles.firstAngle;
    }

    private double distanceTraveled() {
        int ticksLeft = leftDrive.getCurrentPosition() - startTicksLeft;
        int ticksRight = rightDrive.getCurrentPosition() - startTicksRight;
        double ticks = (ticksLeft + ticksRight) / 2;
        return ticks / ticksPerInch;
    }

    private void resetDistance() {
        startTicksLeft = leftDrive.getCurrentPosition();
        startTicksRight = rightDrive.getCurrentPosition();
    }
}
