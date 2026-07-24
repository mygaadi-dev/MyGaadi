using Microsoft.EntityFrameworkCore;
using PanCardAddharCarVerification_Server.Models;

namespace PanCardAddharCarVerification_Server.Services
{
    public class OtpService
    {

        private readonly KycDbContext _context;

        public OtpService(KycDbContext context)
        {
            _context = context;
        }

        public string GenerateOtp()
        {
            Random random = new Random();
            return random.Next(100000, 999999).ToString();
        }


        public async Task SaveOtp(
            string panNumber,
            string otp)
        {

            var record = new OtpRegistry
            {

                PanNumber = panNumber,

                Otp = otp,

                ExpiryTime =
                DateTime.Now.AddMinutes(5),

                Verified = false

            };

            _context.OtpRegistries.Add(record);

            await _context.SaveChangesAsync();

        }

        public async Task<bool> VerifyOtp(
            string panNumber,
            string otp)
        {
            var record =
            await _context.OtpRegistries.Where(x => x.PanNumber == panNumber)
            .OrderByDescending(x => x.Id).FirstOrDefaultAsync();

            if (record == null)
                return false;

            if (record.Otp != otp)
                return false;

            if (record.ExpiryTime < DateTime.Now)
                return false;

            record.Verified = true;

            await _context.SaveChangesAsync();

            return true;
        }

    }
}
