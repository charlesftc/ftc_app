package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.AnalogInput;
import com.qualcomm.robotcore.hardware.CRServo;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.Gamepad;
import com.qualcomm.robotcore.util.ElapsedTime;
import com.qualcomm.robotcore.util.Range;

import static java.lang.Double.NaN;

public class ServoElbow {
    private enum ControlMode {
        PWR_CONTROL, POS_CONTROL;
    }

    private ElapsedTime runtime = new ElapsedTime();
    private LinearOpMode opmode;
    private Gamepad gamepad;
    private CRServo elbowLeft;
    private CRServo elbowRight;
    private AnalogInput pot;
    private DPSCalculator dpsCalc;
    private ControlMode controlMode = ControlMode.PWR_CONTROL;
    //private double maxDPS = 30;    //private double velKP = 0.03;
    private double kP = 0.05;
    private double kI = 0.000005;
    private double integral = 0;
    private double maxIntegral = 0.1;
    private double maxPosPower = 0.7;

    private double goal = NaN;
    private boolean busy = false;

    private double storedAngle;
    private boolean prevX = false;
    private boolean prevY = false;

    /*private double storedPos;
    private boolean prevX = false;
    private boolean prevY = false;
    private boolean prevA = false;
    private boolean prevB = false;
    private boolean prevUp = false;
    private boolean prevDown = false;
    private boolean prevLeft = false;
    private boolean prevRight = false;*/

    private double prevTime;

    public ServoElbow(LinearOpMode opmode, Gamepad gamepad) {
        this.opmode = opmode;
        this.gamepad = gamepad;

        elbowLeft = opmode.hardwareMap.get(CRServo.class, "left_elbow");
        elbowRight = opmode.hardwareMap.get(CRServo.class, "right_elbow");
        elbowLeft.setDirection(DcMotorSimple.Direction.FORWARD);
        elbowRight.setDirection(DcMotorSimple.Direction.REVERSE);

        pot = opmode.hardwareMap.get(AnalogInput.class, "pot");

        dpsCalc = new DPSCalculator(pot, 0.1);
        dpsCalc.start();
    }

    public void control(double power) {
        if (Math.abs(power) > 0.01) {
            goal = NaN;
        }

        if (!Double.isNaN(goal)) {
            positionControl(goal);
        } else {
            stickControl(power);
        }

        /*if (prevX && !gamepad.x) {
            storedAngle = getAngle();
        } else if (prevY && !gamepad.y) {
            goal = storedAngle;
        }

        prevX = gamepad.x;
        prevY = gamepad.y;*/

        //opmode.telemetry.addData("ServoElbow", "kP %f, kI %f, maxI %f, i %f", kP, kI * 1000, maxIntegral, integral);
        //opmode.telemetry.update();
    }

    private void positionControl(double pos) {
        double curTime = runtime.milliseconds();
        double elapsed = curTime - prevTime;
        prevTime = curTime;

        double errorPos = goal - dpsCalc.getAngle();

        double errorArea = Range.clip(errorPos, -3, 3) * elapsed;

        if (Math.abs(errorPos) > 1) {
            integral += errorArea * kI;
        }

        integral = Range.clip(integral, -maxIntegral, maxIntegral);

        double power = Range.clip((-errorPos * kP) + -integral, -maxPosPower, maxPosPower);

        elbowLeft.setPower(power);
        elbowRight.setPower(power);

        if (Math.abs(errorPos) < 3) {
            busy = false;
        }

        //opmode.telemetry.addData("Error", "error %f", errorPos);
    }

    private void stickControl(double power) {
        if (Math.abs(power) < 0.01) {
            if (controlMode != ControlMode.POS_CONTROL) {
                controlMode = ControlMode.POS_CONTROL;
                goal = dpsCalc.getAngle();
                integral = 0;
                prevTime = runtime.milliseconds();
            }

            positionControl(goal);
        } else {
            if (controlMode != ControlMode.PWR_CONTROL) {
                controlMode = ControlMode.PWR_CONTROL;
            }

            elbowLeft.setPower(power);
            elbowRight.setPower(power);
        }
    }

    public double getAngle() {
        return dpsCalc.getAngle();
    }

    public void setGoal(double angle) {
        goal = angle;
        busy = !Double.isNaN(angle);
    }

    public boolean isBusy() {
        return busy;
    }

    public void killThread() {
        dpsCalc.end();
    }
}
