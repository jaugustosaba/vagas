using Microsoft.EntityFrameworkCore;

namespace Vagas.Models {

    public class VagasContext: DbContext {
        public DbSet<Applicant> Applicants { get; set; }
        public DbSet<Job> Jobs { get; set; }
        public DbSet<Application> Applications { get; set; }
        public DbSet<Level> Levels { get; set; }
        public DbSet<Location> Locations { get; set; }
        public DbSet<Distance> Distances { get; set; }

        public VagasContext(DbContextOptions<VagasContext> options): base(options)
        {
        }

        protected override void OnConfiguring(DbContextOptionsBuilder options) {
            
        }
        protected override void OnModelCreating(ModelBuilder modelBuilder)
        {
            var app = modelBuilder.Entity<Application>();
            app.HasKey(c => new { c.ApplicantId, c.JobId });
            app.Navigation(app => app.Job);
            app.Navigation(app => app.Applicant);
            
            
            modelBuilder.Entity<Distance>()
                .HasKey(c => new { c.FromLocationId, c.ToLocationId });
        }

    }

}