using Microsoft.EntityFrameworkCore;
using PanCardAddharCarVerification_Server.Models;

namespace PanCardAddharCarVerification_Server.Services
{
    public class PanService
    {

        private readonly KycDbContext _context;


        public PanService(KycDbContext context)
        {
            _context = context;
        }



        public async Task<PanRegistry?> FindPan(
            string panNumber)
        {

            return await _context.PanRegistries
                .FirstOrDefaultAsync(
                    x => x.PanNumber == panNumber
                );

        }



        public bool IsValidPan(string pan)
        {

            if (string.IsNullOrEmpty(pan))
                return false;


            return pan.Length == 10;

        }
    }
}
