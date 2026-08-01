using Microsoft.EntityFrameworkCore;
using PanCardAddharCarVerification_Server.Models;
using PanCardAddharCarVerification_Server.Services;

namespace PanCardAddharCarVerification_Server
{
    public class Program
    {
        public static void Main(string[] args)
        {
            var builder = WebApplication.CreateBuilder(args);
            var connectionString = builder.Configuration.GetConnectionString("KycDbContext") ?? throw new InvalidOperationException("Connection string 'KycDbContext' not found.");

            builder.Services.AddDbContext<KycDbContext>(options => options.UseSqlServer(connectionString));

            builder.Services.AddScoped<PanService>();

            builder.Services.AddScoped<OtpService>();

            builder.Services.AddScoped<EmailService>(); // <-- Add this line

            // Add services to the container.

            builder.Services.AddControllers();
            // Learn more about configuring OpenAPI at https://aka.ms/aspnet/openapi
            builder.Services.AddOpenApi();

            
            builder.Services.AddCors(options =>
            {
                options.AddPolicy("policy", build =>
                {
                    // Use the built-in 'AllowAny' methods for a complete wildcard setup
                    build.AllowAnyOrigin()
                         .AllowAnyMethod()
                         .AllowAnyHeader(); // <-- This is what you were missing
                });
            });



            var app = builder.Build();

            // Configure the HTTP request pipeline.
            if (app.Environment.IsDevelopment())
            {
                app.MapOpenApi();
            }

            app.UseHttpsRedirection();
            app.UseCors("policy");
            app.UseAuthorization();


            app.MapControllers();

            app.Run();
        }
    }
}
