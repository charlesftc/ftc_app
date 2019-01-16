package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.hardware.CRServo;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.Gamepad;
import com.qualcomm.robotcore.util.ElapsedTime;
import com.qualcomm.robotcore.util.Range;

public class Grabber {

    //private ElapsedTime runtime = new ElapsedTime();
    private Teleop1 opmode;
    private Gamepad gamepad;
    private CRServo leftGrabber;
    private CRServo rightGrabber;
    double power = 0.8;
    boolean grabbing = false;
    boolean releasing = false;
    boolean prevLeftBumper = false;
    boolean prevRightBumper = false;

    public Grabber(Teleop1 opmode, Gamepad gamepad) {
        this.opmode = opmode;
        this.gamepad = gamepad;

        leftGrabber = opmode.hardwareMap.get(CRServo.class, "left_grabber");
        rightGrabber = opmode.hardwareMap.get(CRServo.class, "right_grabber");
        leftGrabber.setDirection(DcMotorSimple.Direction.FORWARD);
        leftGrabber.setDirection(DcMotorSimple.Direction.REVERSE);
    }

    public void execute() {
        //if (((prevLeftBumper && !gamepad.left_bumper) || (prevRightBumper && !gamepad.right_bumper)) && leftGrabber.getPower() > 0.3) {
        if (gamepad.x) {
            leftGrabber.setPower(0);
            rightGrabber.setPower(0);
        } else if (gamepad.left_bumper) {
            leftGrabber.setPower(-power);
            rightGrabber.setPower(-power);
        } else if (gamepad.right_bumper) {
            leftGrabber.setPower(power);
            rightGrabber.setPower(power);
        }

        opmode.telemetry.addData("grabber", "power %f", leftGrabber.getPower());

        /*if (prevLeftBumper && !gamepad.left_bumper && leftGrabber.getPower() > 0.1 && grabbing) {
            leftGrabber.setPower(0);
            rightGrabber.setPower(0);
            releasing = false;
            grabbing = false;
            opmode.telemetry.addData("grabber", "case 3");
        } else if (prevLeftBumper && !gamepad.left_bumper && leftGrabber.getPower() > 0.1 && releasing) {
            leftGrabber.setPower(-power);
            rightGrabber.setPower(-power);
            releasing = false;
            grabbing = true;
            opmode.telemetry.addData("grabber", "case 5");
        } else if (prevLeftBumper && !gamepad.left_bumper && leftGrabber.getPower() < 0.1) {
            leftGrabber.setPower(-power);
            rightGrabber.setPower(-power);
            releasing = false;
            grabbing = true;
            opmode.telemetry.addData("grabber", "case 1");
        }

        if (prevRightBumper && !gamepad.right_bumper && leftGrabber.getPower() > 0.1 && releasing) {
            leftGrabber.setPower(0);
            rightGrabber.setPower(0);
            releasing = false;
            grabbing = false;
            opmode.telemetry.addData("grabber", "case 4");
            //opmode.telemetry.addData("grabber", "power %f", leftGrabber.getPower());
        } else if (prevRightBumper && !gamepad.right_bumper && leftGrabber.getPower() > 0.1 && grabbing) {
            leftGrabber.setPower(power);
            rightGrabber.setPower(power);
            releasing = true;
            grabbing = false;
            opmode.telemetry.addData("grabber", "case 6");
        } else if (prevRightBumper && !gamepad.right_bumper && leftGrabber.getPower() < 0.1) {
            leftGrabber.setPower(power);
            rightGrabber.setPower(power);
            releasing = true;
            grabbing = false;
            opmode.telemetry.addData("grabber", "case 2");
        }*/

        prevLeftBumper = gamepad.left_bumper;
        prevRightBumper = gamepad.right_bumper;

        //opmode.telemetry.addData("grabber", "grabbing, releasing", grabbing, releasing);
        opmode.telemetry.update();
    }
}
