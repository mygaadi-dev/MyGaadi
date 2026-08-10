using System;
using System.Collections.Generic;

namespace PanCardAddharCarVerification_Server.Models;

public partial class PanRegistry
{
    public int Id { get; set; }

    public string PanNumber { get; set; } = null!;

    public string? HolderName { get; set; }

    public string? MobileNumber { get; set; }

    public string? Email { get; set; } // Added Email Property
    
    public string? Status { get; set; }

    public bool? AddharLinked { get; set; }

    public DateTime? CreatedAt { get; set; }
}



