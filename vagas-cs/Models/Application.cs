using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;


namespace Vagas.Models {

    [Table("application")]
    public class Application {
        [Key]
        [Column("applicant_id")]
        public long ApplicantId { get; set; }

        [Key]
        [Column("job_id")]
        public long JobId { get; set; }

        public Applicant Applicant { get; set;}

        public Job Job { get; set; }
    }

}