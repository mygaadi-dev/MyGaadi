using System;
using System.Collections.Generic;

namespace PanCardAddharCarVerification_Server.Models;

public partial class AadhaarRegistry
{
    public long Id { get; set; }

    public string AadhaarNumber { get; set; } = null!;

    public string? HolderName { get; set; }

    public DateOnly? Dob { get; set; }

    public string? Gender { get; set; }

    public string? Address { get; set; }

    public string? MobileNumber { get; set; }

    public string? Email { get; set; }

    public string? Status { get; set; }

    public bool? PanLinked { get; set; }

    public DateTime? CreatedAt { get; set; }
}
