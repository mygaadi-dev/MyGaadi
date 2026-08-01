using System;
using System.Collections.Generic;

namespace PanCardAddharCarVerification_Server.Models;

public partial class OtpRegistry
{
    public int Id { get; set; }

    public string PanNumber { get; set; } = null!;

    public string Otp { get; set; } = null!;

    public DateTime ExpiryTime { get; set; }

    public bool? Verified { get; set; }
}
