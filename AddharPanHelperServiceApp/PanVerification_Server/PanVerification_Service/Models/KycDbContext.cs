using System;
using System.Collections.Generic;
using Microsoft.EntityFrameworkCore;

namespace PanCardAddharCarVerification_Server.Models;

public partial class KycDbContext : DbContext
{
    public KycDbContext()
    {
    }

    public KycDbContext(DbContextOptions<KycDbContext> options)
        : base(options)
    {
    }

    public virtual DbSet<AadhaarRegistry> AadhaarRegistries { get; set; }

    public virtual DbSet<OtpRegistry> OtpRegistries { get; set; }

    public virtual DbSet<PanRegistry> PanRegistries { get; set; }

    protected override void OnConfiguring(DbContextOptionsBuilder optionsBuilder)
#warning To protect potentially sensitive information in your connection string, you should move it out of source code. You can avoid scaffolding the connection string by using the Name= syntax to read it from configuration - see https://go.microsoft.com/fwlink/?linkid=2131148. For more guidance on storing connection strings, see https://go.microsoft.com/fwlink/?LinkId=723263.
        => optionsBuilder.UseSqlServer("Data Source=(LocalDB)\\MSSQLLocalDB;Initial Catalog=kyc_db;Integrated Security=True;Pooling=False;Encrypt=True;Trust Server Certificate=False");

    protected override void OnModelCreating(ModelBuilder modelBuilder)
    {
        modelBuilder.Entity<AadhaarRegistry>(entity =>
        {
            entity.HasKey(e => e.Id).HasName("PK__aadhaar___3213E83FC3237CB1");

            entity.ToTable("aadhaar_registry");

            entity.HasIndex(e => e.AadhaarNumber, "UQ__aadhaar___2ED28027D281ADFB").IsUnique();

            entity.Property(e => e.Id).HasColumnName("id");
            entity.Property(e => e.AadhaarNumber)
                .HasMaxLength(12)
                .IsUnicode(false)
                .HasColumnName("aadhaar_number");
            entity.Property(e => e.Address)
                .HasMaxLength(255)
                .IsUnicode(false)
                .HasColumnName("address");
            entity.Property(e => e.CreatedAt)
                .HasDefaultValueSql("(getdate())")
                .HasColumnType("datetime")
                .HasColumnName("created_at");
            entity.Property(e => e.Dob).HasColumnName("dob");
            entity.Property(e => e.Email)
                .HasMaxLength(100)
                .IsUnicode(false)
                .HasColumnName("email");
            entity.Property(e => e.Gender)
                .HasMaxLength(10)
                .IsUnicode(false)
                .HasColumnName("gender");
            entity.Property(e => e.HolderName)
                .HasMaxLength(100)
                .IsUnicode(false)
                .HasColumnName("holder_name");
            entity.Property(e => e.MobileNumber)
                .HasMaxLength(15)
                .IsUnicode(false)
                .HasColumnName("mobile_number");
            entity.Property(e => e.PanLinked).HasColumnName("pan_linked");
            entity.Property(e => e.Status)
                .HasMaxLength(20)
                .IsUnicode(false)
                .HasColumnName("status");
        });

        modelBuilder.Entity<OtpRegistry>(entity =>
        {
            entity.HasKey(e => e.Id).HasName("PK__otp_regi__3213E83FBC748878");

            entity.ToTable("otp_registry");

            entity.Property(e => e.Id).HasColumnName("id");
            entity.Property(e => e.ExpiryTime)
                .HasColumnType("datetime")
                .HasColumnName("expiry_time");
            entity.Property(e => e.Otp)
                .HasMaxLength(6)
                .IsUnicode(false)
                .HasColumnName("otp");
            entity.Property(e => e.PanNumber)
                .HasMaxLength(10)
                .IsUnicode(false)
                .HasColumnName("pan_number");
            entity.Property(e => e.Verified)
                .HasDefaultValue(false)
                .HasColumnName("verified");
        });

        modelBuilder.Entity<PanRegistry>(entity =>
        {
            entity.HasKey(e => e.Id).HasName("PK__pan_regi__3213E83FA3500ECA");

            entity.ToTable("pan_registry");

            entity.HasIndex(e => e.MobileNumber, "UQ__pan_regi__30462B0FDA782A36").IsUnique();

            entity.HasIndex(e => e.PanNumber, "UQ__pan_regi__9C44595017CC7A97").IsUnique();

            entity.Property(e => e.Id).HasColumnName("id");
            entity.Property(e => e.AddharLinked).HasColumnName("addhar_linked");
            entity.Property(e => e.CreatedAt)
                .HasDefaultValueSql("(getdate())")
                .HasColumnType("datetime")
                .HasColumnName("created_at");
            entity.Property(e => e.HolderName)
                .HasMaxLength(100)
                .IsUnicode(false)
                .HasColumnName("holder_name");
            entity.Property(e => e.MobileNumber)
                .HasMaxLength(15)
                .IsUnicode(false)
                .HasColumnName("mobile_number");
            entity.Property(e => e.PanNumber)
                .HasMaxLength(10)
                .IsUnicode(false)
                .HasColumnName("pan_number");
            // Inside OnModelCreating, inside the modelBuilder.Entity<PanRegistry> block, add:
            entity.Property(e => e.Email)
                .HasMaxLength(100)
                .IsUnicode(false)
                .HasColumnName("email");
            entity.Property(e => e.Status)
                .HasMaxLength(20)
                .IsUnicode(false)
                .HasColumnName("status");
        });

        OnModelCreatingPartial(modelBuilder);
    }

    partial void OnModelCreatingPartial(ModelBuilder modelBuilder);
}
