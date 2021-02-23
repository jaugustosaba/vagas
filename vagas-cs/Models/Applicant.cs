using System.ComponentModel.DataAnnotations.Schema;

namespace Vagas.Models {

    [Table("applicant")]
    public class Applicant {
        [Column("id")]
        public long Id { get; set;}

        [Column("name")]
        public string Name { get; set; }

        [Column("profession")]
        public string Profession { get; set; }

        [Column("location_id")]
        public long LocationId { get; set; }

        [Column("level_id")]
        public long LevelId { get; set; }

        public Location Location {get; set;}

        public Level Level { get; set; }
    }

}