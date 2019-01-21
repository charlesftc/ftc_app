package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.Gamepad;

import static java.lang.Double.NaN;

public class Slide {
    private enum ControlMode {
        PWR_CONTROL, POS_CONTROL;
    }

    //private ElapsedTime runtime = new ElapsedTime();
    private Teleop1 opmode;
    private Gamepad gamepad;
    private DcMotor slideMotor;
    private ControlMode controlMode = ControlMode.POS_CONTROL;

    private int origPos;
    private int ticksPerLength = 800;
    private double holdPower = 0.1;
    private double posPower = 0.8;
    private double goal = NaN;

    private double storedPos;
    private boolean prevA = false;
    private boolean prevB = false;
    ///private RPSCalculator rpsCalc;

    public Slide(Teleop1 opmode, Gamepad gamepad) {
        this.opmode = opmode;
        this.gamepad = gamepad;

        slideMotor = opmode.hardwareMap.get(DcMotor.class, "slide_motor");
        slideMotor.setDirection(DcMotor.Direction.FORWARD);
        slideMotor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);

        origPos = slideMotor.getCurrentPosition();
        //slideMotor.setMode(DcMotor.RunMode.RUN_USING_ENCODER);

        //rpsCalc = new RPSCalculator(shoulderMotor, ticksPerRev, 0.2);
        //rpsCalc.start();
    }

    public void control(float power) {
        if (Math.abs(power) > 0.01) {
            goal = NaN;
        }

        if (!Double.isNaN(goal)) {
            positionControl(goal, posPower);
        } else {
            triggerControl(power);
        }

        if (prevA && !gamepad.a) {
            storedPos = getPosition();
        } else if (prevB && !gamepad.b) {
            goal = storedPos;
        }

        prevA = gamepad.a;
        prevB = gamepad.b;
    }

    public void triggerControl(float power) {
        if (Math.abs(power) < 0.01) {
            if (controlMode != ControlMode.POS_CONTROL) {
                positionControl(getPosition(), holdPower);
            }
        } else {
            if (controlMode != ControlMode.PWR_CONTROL) {
                controlMode = ControlMode.PWR_CONTROL;
                slideMotor.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
            }

            slideMotor.setPower(power);
        }

        opmode.telemetry.addData("Slide", "curPos %d, orig pos %d, diff %d", (int) (getPosition() * ticksPerLength), origPos, (int) (getPosition() * ticksPerLength) - origPos);
        opmode.telemetry.update();
    }

    private void positionControl(double pos, double power) {
        controlMode = ControlMode.POS_CONTROL;
        slideMotor.setMode(DcMotor.RunMode.RUN_TO_POSITION);
        slideMotor.setPower(power);
        int ticks = (int) (pos * ticksPerLength);
        slideMotor.setTargetPosition(ticks);
    }

    private double getPosition() {
        return (double) slideMotor.getCurrentPosition() / ticksPerLength;
    }

    public void setGoal(double pos) {
        goal = pos;
    }
}
