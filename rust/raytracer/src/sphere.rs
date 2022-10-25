use crate::{QuadraticEquation, Vec3};
use crate::aabb::AABB;
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
}

impl<M: Material> Hittable for Sphere<M> {
    fn hit(&self, ray: &Ray, t_min: f64, t_max: f64) -> Option<HitRecord> {
        let oc = ray.origin - self.center;
        let eq = QuadraticEquation::new(
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

    fn bounding_box(&self) -> Option<AABB> {
        Some(AABB {
            min: self.center - Vec3::new(self.radius, self.radius, self.radius),
            max: self.center + Vec3::new(self.radius, self.radius, self.radius),
        })
    }

    fn get_normal(&self, point: &Vec3) -> Vec3 {
        (*point - self.center) / self.radius
    }

    fn get_coordinates(&self, point: &Vec3) -> (f64, f64) {
        let theta = (-point.y).acos();
        let phi = (-point.z).atan2(point.x) + std::f64::consts::PI;
        let u = phi / (2.0 * std::f64::consts::PI);
        let v = theta / std::f64::consts::PI;
        (u, v)
    }

    fn get_material(&self) -> &dyn Material {
        &self.material
    }
}
