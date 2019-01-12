package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.hardware.AnalogInput;
import com.qualcomm.robotcore.hardware.CRServo;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.Gamepad;
import com.qualcomm.robotcore.util.ElapsedTime;
import com.qualcomm.robotcore.util.Hardware;
import com.qualcomm.robotcore.util.Range;

public class Elbow {

    // Declare OpMode members.
    private ElapsedTime runtime = new ElapsedTime();
    private CRServo elbowLeft;
    private CRServo elbowRight;
    private AnalogInput pot;
    private Teleop1 opmode;
    private Gamepad gamepad;
    private DPSCalculator dpsCalc = new DPSCalculator(pot, 0.2);
    private double kP = 0.03;

    public Elbow(Teleop1 opmode, Gamepad gamepad) {
        this.opmode = opmode;
        this.gamepad = gamepad;

        elbowLeft = opmode.hardwareMap.get(CRServo.class, "left_elbow");
        elbowRight = opmode.hardwareMap.get(CRServo.class, "right_elbow");
        elbowLeft.setDirection(DcMotorSimple.Direction.FORWARD);
        elbowRight.setDirection(DcMotorSimple.Direction.REVERSE);

        pot = opmode.hardwareMap.get(AnalogInput.class, "pot");
    }

    public void execute() {
        double targetDPS = -gamepad.right_stick_y * 30;
        double currentDPS = dpsCalc.getDPS();
        double errorDPS = targetDPS - currentDPS;
        double power = Range.clip(errorDPS * kP, -1, 1);

        elbowLeft.setPower(power);
        elbowRight.setPower(power);

        //opmode.telemetry.addData("Status", "Run Time: " + runtime.toString());
        //opmode.telemetry.addData("Elbow", "power %.2f, stick %.2f, currentDPS %f", power, gamepad.right_stick_y, currentDPS);
        //opmode.telemetry.update();
    }
}
