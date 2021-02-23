using System.ComponentModel.DataAnnotations.Schema;

namespace Vagas.Models {

    [Table("job")]
    public class Job {
        [Column("id")]
        public long Id { get; set; }

        [Column("company")]
        public string Company { get; set; }

        [Column("title")]
        public string Title { get; set; }

        [Column("description")]
        public string Description { get; set; }

        [Column("location_id")]
        public long LocationId { get; set; }

        [Column("level_id")]
        public long LevelId { get; set; }

        public Level Level { get; set; }

        public Location Location { get; set; }

    }

}