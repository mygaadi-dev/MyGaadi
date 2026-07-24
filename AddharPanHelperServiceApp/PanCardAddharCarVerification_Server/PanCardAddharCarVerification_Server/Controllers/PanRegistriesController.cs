using Microsoft.AspNetCore.Mvc;
using PanCardAddharCarVerification_Server.DTOs;
using PanCardAddharCarVerification_Server.Services;

namespace PanCardAddharCarVerification_Server.Controllers;

[Route("api/pan")]
[ApiController]
public class PanController : ControllerBase
{
    private readonly PanService _panService;
    private readonly OtpService _otpService;
    private readonly EmailService _emailService;

    // Injecting the services instead of directly using the DbContext
    public PanController(PanService panService, OtpService otpService, EmailService emailService)
    {
        _panService = panService;
        _otpService = otpService;
        _emailService = emailService;
    }

    [HttpPost("verify")]
    public async Task<IActionResult> VerifyPan([FromBody] PanRequest request)
    {
        // 1. Check if valid format
        if (!_panService.IsValidPan(request.PanNumber))
        {
            return BadRequest(new { success = false, message = "Invalid PAN format." });
        }

        // 2. Find PAN in DB
        var pan = await _panService.FindPan(request.PanNumber);

        if (pan == null)
        {
            return NotFound(new { success = false, code = "PAN_NOT_FOUND", message = "PAN record not found" });
        }

        if (string.IsNullOrEmpty(pan.Email))
        {
            return BadRequest(new { success = false, code = "EMAIL_NOT_FOUND", message = "No registered email found for this PAN." });
        }

        // 3. Generate and Save OTP
        var otp = _otpService.GenerateOtp();
        await _otpService.SaveOtp(pan.PanNumber, otp);

        // 4. Send Email
        try
        {
            await _emailService.SendOtpEmailAsync(pan.Email, otp);
            
            // Create a masked email for response (e.g., s******@gmail.com)
            var emailParts = pan.Email.Split('@');
            var maskedEmail = $"{emailParts[0][0]}******@{emailParts[1]}";

            return Ok(new
            {
                success = true,
                code = "OTP_SENT",
                message = "OTP sent to registered email address",
                data = new { maskedEmail = maskedEmail, expiresIn = 300 }
            });
        }
        catch (Exception ex)
        {
            // Log the exception in a real app
            Console.WriteLine(ex.Message);
            return StatusCode(500, new { success = false, message = "Failed to send OTP email. Please try again later." });
        }
    }

    [HttpPost("verify-otp")]
    public async Task<IActionResult> VerifyOtp([FromBody] OtpRequest request)
    {
        var isValid = await _otpService.VerifyOtp(request.PanNumber, request.OTP);

        if (!isValid)
        {
            return BadRequest(new { success = false, code = "INVALID_OTP", message = "Invalid or expired OTP" });
        }

        var pan = await _panService.FindPan(request.PanNumber);

        return Ok(new
        {
            success = true,
            code = "PAN_VERIFIED",
            message = "PAN verification completed",
            data = new
            {
                panNumber = pan!.PanNumber,
                holderName = pan.HolderName,
                status = pan.Status,
                kycStatus = "COMPLETED"
            }
        });
    }
}