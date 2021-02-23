using System.ComponentModel.DataAnnotations.Schema;

namespace Vagas.Models {

    [Table("location")]
    public class Location {
        [Column("id")]
        public long Id { get; set; }

        [Column("name")]
        public string Name { get; set; }
    }

}