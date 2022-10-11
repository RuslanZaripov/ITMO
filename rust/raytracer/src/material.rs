use crate::{Ray, Vec3};
use crate::hittable::HitRecord;

trait Material {
    fn scatter(&self, ray_in: &Ray, hit_record: &HitRecord) -> Option<(Vec3, Ray)>;
}