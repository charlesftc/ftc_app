package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.hardware.AnalogInput;
import com.qualcomm.robotcore.hardware.CRServo;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.Gamepad;
import com.qualcomm.robotcore.util.ElapsedTime;
import com.qualcomm.robotcore.util.Range;

public class Elbow {
    private enum CommandMode {
        PWR_CONTROL, POS_CONTROL;
    }

    private ElapsedTime runtime = new ElapsedTime();
    private Teleop1 opmode;
    private Gamepad gamepad;
    private CRServo elbowLeft;
    private CRServo elbowRight;
    private AnalogInput pot;
    private DPSCalculator dpsCalc;
    private CommandMode commandMode = CommandMode.PWR_CONTROL;
    //private double maxDPS = 30;
    //private double velKP = 0.03;
    private double kP = 0.05;
    private double kI = 0.000005;
    private double integral = 0;
    private double maxIntegral = 0.1;
    private double maxPosPower = 0.7;

    private double targetPos;

    private double storedPos;
    private boolean prevX = false;
    private boolean prevY = false;
    private boolean prevA = false;
    private boolean prevB = false;
    private boolean prevUp = false;
    private boolean prevDown = false;
    private boolean prevLeft = false;
    private boolean prevRight = false;

    private double prevTime;

    public Elbow(Teleop1 opmode, Gamepad gamepad) {
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

    private void positionControl(double pos) {
        double curTime = runtime.milliseconds();
        double elapsed = curTime - prevTime;
        prevTime = curTime;

        double errorPos = targetPos - dpsCalc.getPos();

        double errorArea = Range.clip(errorPos, -3, 3) * elapsed;

        if (Math.abs(errorPos) > 1) {
            integral += errorArea * kI;
        }

        integral = Range.clip(integral, -maxIntegral, maxIntegral);

        double power = Range.clip((-errorPos * kP) + -integral, -maxPosPower, maxPosPower);

        elbowLeft.setPower(power);
        elbowRight.setPower(power);

        opmode.telemetry.addData("Error", "error %f", errorPos);
    }

    private void powerControl(double power) {
        elbowLeft.setPower(power);
        elbowRight.setPower(power);
    }

    /*private void velocityControl(double commandVel) {
        double targetDPS = commandVel * maxDPS;
        double currentDPS = dpsCalc.getDPS();
        double errorDPS = targetDPS - currentDPS;
        double power = Range.clip(errorDPS * velKP, -1, 1);

        elbowLeft.setPower(power);
        elbowRight.setPower(power);

        //opmode.telemetry.addData("Status", "Run Time: " + runtime.toString());
        opmode.telemetry.addData("Elbow", "power %.2f, targetDPS %.2f, currentDPS %f, position %f", power, targetDPS, currentDPS, dpsCalc.getPos());
        opmode.telemetry.update();
    }*/

    public void execute() {
        double commandPower = -gamepad.right_stick_y;

        double adjustAmount = 0.000001;
        if (prevX && !gamepad.x) {
            storedPos = dpsCalc.getPos();
        } else if (prevY && !gamepad.y) {
            targetPos = storedPos;
        }

        if (prevA && !gamepad.a) {
            kP += 0.001;
        } else if (prevB && !gamepad.b) {
            kP -= 0.001;
        }

        if (prevLeft && !gamepad.dpad_left) {
            kI += adjustAmount;
        } else if (prevUp && !gamepad.dpad_up) {
            kI -= adjustAmount;
        }

        if (prevDown && !gamepad.dpad_down) {
            maxIntegral += 0.005;
        } else if (prevRight && !gamepad.dpad_right) {
            maxIntegral -= 0.005;
        }

        prevX = gamepad.x;
        prevY = gamepad.y;
        prevA = gamepad.a;
        prevB = gamepad.b;

        prevUp = gamepad.dpad_up;
        prevDown = gamepad.dpad_down;
        prevLeft = gamepad.dpad_left;
        prevRight = gamepad.dpad_right;

        if (Math.abs(commandPower) < 0.01) {
            if (commandMode != CommandMode.POS_CONTROL) {
                commandMode = CommandMode.POS_CONTROL;
                targetPos = dpsCalc.getPos();
                integral = 0;
                prevTime = runtime.milliseconds();
            }

            positionControl(targetPos);
        } else {
            commandMode = CommandMode.PWR_CONTROL;
            powerControl(commandPower);
        }

        //opmode.telemetry.addData("Elbow", "kP %f, kI %f, maxI %f, i %f", kP, kI * 1000, maxIntegral, integral);
        //opmode.telemetry.update();
    }

    public void killThread() {
        dpsCalc.end();
    }
}
