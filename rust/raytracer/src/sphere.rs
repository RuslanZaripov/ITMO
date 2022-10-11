use crate::{Equation, Vec3};
use crate::hittable::{HitRecord, Hittable};
use crate::material::Material;
use crate::ray::Ray;
use crate::utils::{find_first, sq};
use crate::vec3::dot;

#[derive(Debug, Copy, Clone)]
pub struct Sphere<M: Material> {
    pub center: Vec3,
    pub radius: f64,
    pub material: M,
}

impl<M: Material> Sphere<M> {
    pub fn new(center: Vec3, radius: f64, material: M) -> Self {
        Self { center, radius, material }
    }

    fn get_hit_record(&self, ray: &Ray, root: f64) -> Option<HitRecord> {
        let point = ray.at(root);
        let outward_normal = (point - self.center) / self.radius;
        let front_face = dot(&ray.dir, &outward_normal) < 0.0;
        let normal = if front_face { outward_normal } else { -outward_normal };
        return Some(HitRecord {
            factor: root,
            point,
            normal,
            material: &self.material,
            front_face
        });
    }
}

impl<M: Material> Hittable for Sphere<M> {
    fn hit(&self, ray: &Ray, t_min: f64, t_max: f64) -> Option<HitRecord> {
        let oc = ray.origin - self.center;
        let eq = Equation::new(
            dot(&ray.dir, &ray.dir),
            2.0 * dot(&oc, &ray.dir),
            dot(&oc, &oc) - sq(self.radius),
        );
        return match eq.solve() {
            Some((t1, t2)) => {
                match find_first(
                    vec![t1, t2],
                    |&t| t_min < t && t < t_max,
                ) {
                    Some(t) => self.get_hit_record(ray, t),
                    None => None,
                }
            }
            None => None
        };
    }
}
