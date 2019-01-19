package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.Gamepad;
import com.qualcomm.robotcore.util.ElapsedTime;
import com.qualcomm.robotcore.util.Range;

public class Slide {
    //private ElapsedTime runtime = new ElapsedTime();
    private Teleop1 opmode;
    private Gamepad gamepad;
    private DcMotor slideMotor;
    ///private RPSCalculator rpsCalc;

    public Slide(Teleop1 opmode, Gamepad gamepad) {
        this.opmode = opmode;
        this.gamepad = gamepad;

        slideMotor = opmode.hardwareMap.get(DcMotor.class, "slide_motor");
        slideMotor.setDirection(DcMotor.Direction.FORWARD);
        slideMotor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        //slideMotor.setMode(DcMotor.RunMode.RUN_USING_ENCODER);

        //rpsCalc = new RPSCalculator(shoulderMotor, ticksPerRev, 0.2);
        //rpsCalc.start();
    }

    public void execute() {
        double power = gamepad.right_trigger - gamepad.left_trigger;
        //double targetVel = -gamepad.left_stick_y * 0.2;
        //double currentVel = rpsCalc.getRPS();
        //double errorVel = targetVel - currentVel;
        //double commandVel = currentVel + Range.clip(errorVel, -0.04, 0.04);
        //commandVel = targetVel * 10;2
        //shoulderMotor.setPower(commandVel * 128 / 15);
        slideMotor.setPower(power);
    }
}
