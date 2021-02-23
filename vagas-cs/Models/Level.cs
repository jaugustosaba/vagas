using System.ComponentModel.DataAnnotations.Schema;

namespace Vagas.Models {

    [Table("level")]
    public class Level {
        [Column("id")]
        public long Id { get; set; }

        [Column("name")]
        public string Name { get; set; }
    }

}