package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.Gamepad;
import com.qualcomm.robotcore.util.ElapsedTime;
import com.qualcomm.robotcore.util.Range;

public class Shoulder {

    //private ElapsedTime runtime = new ElapsedTime();
    private Teleop1 opmode;
    private Gamepad gamepad;
    private DcMotor shoulderMotor;
    private RPSCalculator rpsCalc;

    public Shoulder(Teleop1 opmode, Gamepad gamepad) {
        int ticksPerRev = 14336;
        this.opmode = opmode;
        this.gamepad = gamepad;

        shoulderMotor = opmode.hardwareMap.get(DcMotor.class, "shoulder_motor");
        shoulderMotor.setDirection(DcMotor.Direction.FORWARD);
        shoulderMotor.setMode(DcMotor.RunMode.RUN_USING_ENCODER);

        rpsCalc = new RPSCalculator(shoulderMotor, ticksPerRev, 0.2);
        rpsCalc.start();
    }

    public void execute() {
        double targetVel = -gamepad.left_stick_y * 0.2;
        double currentVel = rpsCalc.getRPS();
        double errorVel = targetVel - currentVel;
        double commandVel = currentVel + Range.clip(errorVel, -0.04, 0.04);
        //commandVel = targetVel * 10;2

        //shoulderMotor.setPower(commandVel * 128 / 15);
        shoulderMotor.setPower(commandVel / 0.17);
    }
}
